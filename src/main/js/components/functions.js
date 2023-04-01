export function classNames() {
	// accept both varargs and arrays
	const args = Array.prototype.slice.call(arguments)
	const classes = args.length === 1 && Array.isArray(args[0]) ? args[0] : args

	const className = classes
		.filter(cls => cls !== undefined && cls !== null && cls !== "")
		.join(" ")
	return className ? { className } : null
}
