import { DateTime, Duration, Interval } from "luxon";

export const arrayTo = (length: number): number[] => [ ...Array(length).keys() ]

export const splitByMonth = (start: DateTime, lengthInDays: number): Interval[] => {
	if (lengthInDays === 1)
		return [ Interval.after(start, { days: lengthInDays }) ]

	const lastDayOfEachMonth = arrayTo(12)
		.map(index => ++index)
		.map(month => DateTime.local(start.year, month, 1))
	return Interval
		.after(start, Duration.fromObject({ days: lengthInDays }))
		.splitAt(...lastDayOfEachMonth)
}

