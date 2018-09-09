/**
 * Fast json serializer / deserializer, using standard Java types.
 * According to wikipedia :-) JSON has only a few data types:
 * 	Number (double-precision floating-point format in JavaScript, generally depends on implementation)
 * 	String (double-quoted Unicode, with backslash escaping)
 * 	Boolean (true or false)
 * 	Array (an ordered, comma-separated sequence of values enclosed in square brackets; the values do not need to be of the same type)
 * 	Object (an unordered, comma-separated collection of key:value pairs enclosed in curly braces, with the ':' character separating the key and the value; the keys must be strings and should be distinct from each other)
 * 	null (empty)
 * Not surprisingly, Java also includes these data types.
 * What is shocking is that all the json libraries I know of for java require their *own* custom data types!!!!
 * What a pain in the butt when you want to just pass a map or array of objects around!
 * This library includes a fast json parser which deserializes json text to standard java objects.
 * The parser also provides hooks so you can use your own maps (say FastMap), lists (optionally converted to native []), and Number types (Double, Integer, BigDecimal, BigInteger, etc.).
 * The parser even provides a hook allowing you to optional convert String types (e.g. Date).
 * Conversely, these same types can be serialized back out to json text.
 */
package com.bytelightning.oss.lib.json;