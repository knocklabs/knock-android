"""Fails if R8 renamed any Knock model class or member in a minified consumer build.

The models are decoded by Jackson via reflection, so renamed classes or fields break every API call.
Usage: check_r8_mapping.py <mapping.txt>
"""
import re
import sys

MODELS = "app.knock.client.models."
class_line = re.compile(r"^(\S+) -> (\S+):$")
# "    type name -> new" (field) or "    [1:2:]type name(args)[:3[:4]] -> new" (method)
member_line = re.compile(r"^    (?:\d+:\d+:)?\S+ ([^\s(]+)(?:\([^)]*\))?(?::\d+(?::\d+)?)? -> (\S+)$")

renamed = []
checked = 0
current = None
with open(sys.argv[1]) as mapping:
    for line in mapping:
        line = line.rstrip("\n")
        match = class_line.match(line)
        if match:
            original, obfuscated = match.groups()
            current = original if original.startswith(MODELS) else None
            if current:
                checked += 1
                if original != obfuscated:
                    renamed.append(f"class {original} -> {obfuscated}")
            continue
        if current:
            match = member_line.match(line)
            # Names containing "." are frames R8 inlined from other classes, not members of this class.
            if match and "." not in match.group(1) and match.group(1) != match.group(2):
                renamed.append(f"{current}.{match.group(1)} -> {match.group(2)}")

if checked == 0:
    sys.exit(f"No {MODELS}* classes found in {sys.argv[1]}")
if renamed:
    sys.exit("R8 renamed Knock models:\n  " + "\n  ".join(renamed))
print(f"OK: {checked} Knock model classes kept intact")
