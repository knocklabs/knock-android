package android.util

//@file:JvmName("Log")

class Log {
    companion object {
        @JvmStatic
        fun d(tag: String, msg: String): Int {
            println("DEBUG: $tag: $msg")
            return 0
        }

        @JvmStatic
        fun d(tag: String, msg: String, tr: Throwable): Int {
            println("DEBUG: $tag: $msg");
            return 0
        }

        @JvmStatic
        fun i(tag: String, msg: String): Int {
            println("INFO: $tag: $msg")
            return 0
        }

        @JvmStatic
        fun w(tag: String, msg: String): Int {
            println("WARN: $tag: $msg")
            return 0
        }

        @JvmStatic
        fun e(tag: String, msg: String): Int {
            println("ERROR: $tag: $msg")
            return 0
        } // add other methods if required...
    }
}