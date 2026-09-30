"""Fails if R8 renamed any Knock model class or member in a minified consumer build.

The models are decoded by Jackson via reflection, so renamed classes or fields break every API call.
Usage: check_r8_mapping.py <mapping.txt>
"""
import re
import sys
from collections import defaultdict

MODELS = "app.knock.client.models."
class_line = re.compile(r"^(\S+) -> (\S+):$")
# "    type name -> new" (field) or "    [1:2:]type name(args)[:3[:4]] -> new" (method)
member_line = re.compile(r"^    (?:\d+:\d+:)?\S+ ([^\s(]+)(?:\([^)]*\))?(?::\d+(?::\d+)?)? -> (\S+)$")

renamed_classes = []
targets = defaultdict(lambda: defaultdict(set))  # class -> original member -> names it maps to
current = None
with open(sys.argv[1]) as mapping:
    for line in mapping:
        line = line.rstrip("\n")
        match = class_line.match(line)
        if match:
            original, obfuscated = match.groups()
            current = original if original.startswith(MODELS) else None
            if current:
                targets[current]  # record the class
                if original != obfuscated:
                    renamed_classes.append(f"class {original} -> {obfuscated}")
            continue
        if current:
            match = member_line.match(line)
            # Names containing "." are frames R8 inlined from other classes, not members of this class.
            if match and "." not in match.group(1):
                targets[current][match.group(1)].add(match.group(2))

if not targets:
    sys.exit(f"No {MODELS}* classes found in {sys.argv[1]}")

# Inlining produces extra frames mapping a member to another name; it's only renamed if no entry keeps its name.
renamed_members = [
    f"{cls}.{member} -> {', '.join(sorted(names))}"
    for cls, members in targets.items()
    for member, names in members.items()
    if member not in names
]
renamed = renamed_classes + renamed_members
if renamed:
    sys.exit("R8 renamed Knock models:\n  " + "\n  ".join(renamed))
print(f"OK: {len(targets)} Knock model classes kept intact")
