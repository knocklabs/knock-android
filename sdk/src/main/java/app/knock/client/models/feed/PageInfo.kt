package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PageInfo(
    var before: String? = null,
    var after: String? = null,
    var pageSize: Int = 0,
)
