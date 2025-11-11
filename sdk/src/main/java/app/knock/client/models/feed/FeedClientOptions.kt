package app.knock.client.models.feed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeedClientOptions(
    var before: String? = null,
    var after: String? = null,
    var pageSize: Int? = null,
    var status: FeedItemScope? = null,
    var source: String? = null, // Optionally scope all notifications to a particular source only
    var tenant: String? = null,  // Optionally scope all requests to a particular tenant
    var hasTenant: Boolean? = null, // Optionally scope to notifications with any tenancy or no tenancy
    var archived: FeedItemArchivedScope? = null, // Optionally scope to a given archived status (defaults to `exclude`)
    var triggerData: Map<String, Any>? = null,
    var locale: String? = null,
) {
    /**
     * Merge new options to the exiting ones, if the new ones are nil, only a copy of `self` will be returned
     *
     * @param options the options to merge with the current struct, if they are nil, only a copy of `self` will be returned
     * @return a new struct of type `FeedClientOptions` with the options passed as the parameter merged into it.
     */
    fun mergeOptions(options: FeedClientOptions? = null): FeedClientOptions {
        // initialize a new `mergedOptions` as a copy of `this`
        val mergedOptions = this.copy()

        // check if the passed options are not nil
        if (options == null) {
            return mergedOptions
        }

        // for each one of the properties `not nil` in the parameter `options`, override the ones in the new struct
        if (options.before != null) {
            mergedOptions.before = options.before
        }
        if (options.after != null) {
            mergedOptions.after = options.after
        }
        if (options.pageSize != null) {
            mergedOptions.pageSize = options.pageSize
        }
        if (options.status != null) {
            mergedOptions.status = options.status
        }
        if (options.source != null) {
            mergedOptions.source = options.source
        }
        if (options.tenant != null) {
            mergedOptions.tenant = options.tenant
        }
        if (options.hasTenant != null) {
            mergedOptions.hasTenant = options.hasTenant
        }
        if (options.archived != null) {
            mergedOptions.archived = options.archived
        }
        if (options.triggerData != null) {
            mergedOptions.triggerData = options.triggerData
        }
        if (options.locale != null) {
            mergedOptions.locale = options.locale
        }

        return mergedOptions
    }
}
