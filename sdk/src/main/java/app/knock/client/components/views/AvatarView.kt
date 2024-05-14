package app.knock.client.components.views

@Composable
fun AvatarView(
    imageURLString: String?,
    name: String?,
    backgroundColor: Color = Color.Gray,
    fontSize: Int = 16,
    textColor: Color = Color.Black,
    size: Int = 32
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size.dp)
            .background(backgroundColor, CircleShape)
    ) {
        if (imageURLString != null) {
            val painter = rememberAsyncImagePainter(model = imageURLString)
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(size.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            val initials = generateInitials(name)
            if (initials != null) {
                Text(
                    text = initials,
                    fontSize = fontSize.sp,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun generateInitials(name: String?): String? {
    return name?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
        ?.joinToString("")
}