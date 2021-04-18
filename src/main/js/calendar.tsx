import React, { Dispatch, SetStateAction, useMemo, useState } from 'react'
import { DateTime } from 'luxon'

import {
	Cell,
	Coordinates,
	Entry,
	GriddedEntry,
	Holiday,
	Month,
	Person,
	Theme,
	ThemedYear
} from "./types"
import { arrayTo, splitByMonth } from "./functions";
import { createCalendar } from "./calendarFactory";

const style = require('./calendar.module.css')

export const NO_PERSON = "NONE"

interface SetState<T> extends Dispatch<SetStateAction<T>> {}

interface CalendarProperties {
	year: number
	entries: Entry[]
	holidays: Holiday[]
	people: Person[]
	themes: ThemedYear[]
	setHoveredEntry: SetState<Entry | undefined>
	setSelectedEntry: SetState<Entry | undefined>
}

const Calendar = ({
					  year,
					  entries,
					  holidays,
					  people: teamMembers,
					  themes,
					  setHoveredEntry,
					  setSelectedEntry
				  }: CalendarProperties) => {

	const [ highlight, setHighlight ] = useState<Highlight>({
		cell: undefined,
		entry: undefined,
	})

	const { griddedEntries, gridStyle, months, people } = useMemo(
		() => createCalendar(year, entries, holidays, teamMembers, themes),
		[ year, entries, holidays, teamMembers, themes ])

	return (
		<div
			className={style.grid}
			style={{ ...gridStyle }}
			onMouseOver={event => updateHover(setHighlight, setHoveredEntry, entries, people, event.target)}
			onMouseLeave={__ => updateHover(setHighlight, setHoveredEntry, entries, people)}
			onClick={event => updateSelected(setSelectedEntry, entries, event.target)}
		>
			{months.map((month: Month) => displayMonth(month, highlight))}
			{months
				.flatMap((month: Month) => people.map((person: Person) => ({ month, person })))
				.map(({ month, person }) => displayPerson(month, person, highlight))}
			{arrayTo(31).map(day => displayDayOfMonth(day, highlight))}
			{themes
				.flatMap((themedYear: ThemedYear, yearIndex: number) => themedYear.themes
					.map((theme: Theme, themeIndex: number) => ({ theme, yearIndex, month: months[themeIndex] })))
				.map(({ theme, yearIndex, month }) => displayTheme(theme, yearIndex, month, highlight))}
			{griddedEntries.map(entry => displayEntry(entry, people.map(person => person.abbreviation), highlight))}
		</div>
	)
}

const updateHover = (
	setHighlight: SetState<Highlight>,
	setHoveredEntry: SetState<Entry | undefined>,
	entries: Entry[],
	people: Person[],
	eventTarget?: EventTarget
) => {
	const highlight: Highlight = determineHighlight(entries, people, eventTarget)
	setHighlight(highlight)
	setHoveredEntry(highlight.entry)
}

const determineHighlight = (entries: Entry[], people: Person[], eventTarget?: EventTarget): Highlight => {
	if (!(eventTarget instanceof HTMLElement))
		return {}
	const cell = eventTarget as HTMLElement

	const updateToEntry = Boolean(cell.dataset.entryindex)
	if (updateToEntry) return highlightFromCoordinates(coordinatesFromDomData(cell), entries, people)

	const updateToCell = !cell.classList.contains(style.nonDay)
	if (updateToCell) return highlightFromCoordinates(coordinatesFromDomData(cell), entries, people)

	return {}
}

const updateSelected = (setSelectedEntry: SetState<Entry | undefined>, entries: Entry[], cell: EventTarget) => {
	const index: string | undefined = cell instanceof HTMLElement ? cell.dataset.entryindex : undefined
	const entry: Entry | undefined = index ? entries[parseInt(index)] : undefined
	setSelectedEntry(entry)
}

/*
 * DISPLAY CALENDAR
 */

const displayMonth = (month: Month, highlight: Highlight) => {
	const className = `${style.month} ${(monthToHighlight(month, highlight) ? style.highlighted : "")}`
	const gridArea = month.abbreviation
	return (
		<div key={gridArea} className={className} style={{ gridArea }}>
			{month.name}
		</div>
	)
}

const monthToHighlight = (month: Month, highlight: Highlight): boolean => {
	if (highlight.cell)
		return month.number === highlight.cell?.month
	if (highlight.entry)
		return splitByMonth(highlight.entry.start, highlight.entry.length)
			.map(monthInterval => monthInterval.start.month)
			.includes(month.number)
	return false
}

const displayPerson = (month: Month, person: Person, highlight: Highlight) => {
	const gridArea = `${month.abbreviation}_${person.abbreviation}`
	// TODO use name
	const text = person.abbreviation === NO_PERSON ? "" : person.abbreviation
	const className = style.person + (personToHighlight(month, person, highlight) ? " " + style.highlighted : "")
	return (
		<div key={gridArea} className={className} style={{ gridArea }}>
			<div>{text}</div>
		</div>
	)
}

const personToHighlight = (month: Month, person: Person, highlight: Highlight): boolean => {
	if (!monthToHighlight(month, highlight)) return false
	if (highlight.cell) return person === highlight.cell.person
	if (highlight.entry)
		return highlight.entry.people.length === 0
			? person.abbreviation === NO_PERSON
			: highlight.entry.people
				.map(person => person.abbreviation)
				.includes(person.abbreviation)
	return false
}


const displayDayOfMonth = (day: number, highlight: Highlight) => {
	const className = style.dayOfMonth + (dayOfMonthToHighlight(day, highlight) ? " " + style.highlighted : "")
	return (
		<div key={day} className={className} style={{ gridArea: `d_${day + 1}` }}>
			{day + 1}
		</div>
	)
}

const dayOfMonthToHighlight = (day: number, highlight: Highlight): boolean => {
	if (highlight.cell) return day + 1 === highlight.cell?.day
	if (highlight.entry) {
		return arrayTo(highlight.entry.length)
			.map(day => highlight.entry!.start.plus({ days: day }))
			.map(date => date.day)
			.includes(day + 1)
	}
	return false
}

const displayTheme = (theme: Theme, yearIndex: number, month: Month, highlight: Highlight) => {
	const className = `${style.theme} ${(monthToHighlight(month, highlight) ? style.highlighted : "")}`
	const gridArea = `theme_${yearIndex}_${month.abbreviation}`
	const elementStyle = {
		gridArea,
		color: color(theme.textColor),
		backgroundColor: color(theme.cellColor),
	}
	return (
		<div key={gridArea} className={className} style={elementStyle}>
			{theme.name}
		</div>
	)
}

const color = (color: string): string => {
	switch (color) {
		case "fg":
			return "var(--fg-color)"
		case "bg":
			return "var(--bg-color)"
		default:
			return color
	}
}

const displayEntry = (entry: GriddedEntry, allPeople: string[], highlight: Highlight) => {
	const className = entry.className + " " + style.cell + " " + detectHighlightedEntryClass(entry, allPeople, highlight)
	const data = entry.coordinates
		? coordinatesToDomData(entry.coordinates)
		: {}
	return (
		<div
			key={entry.reactKey}
			className={className}
			style={{ ...entry.gridArea, backgroundColor: entry.color }}
			{...data}
		/>
	)
}

const coordinatesToDomData = (coordinates: Coordinates): any => {
	return {
		'data-month': coordinates.cell?.month,
		'data-day': coordinates.cell?.day,
		'data-person': coordinates.cell?.personIndex,
		'data-entryindex': coordinates.entryIndex,
	}
}

const coordinatesFromDomData = (cell: HTMLElement): Coordinates => cell.dataset.entryindex
	? { entryIndex: parseInt(cell.dataset.entryindex as string) }
	: {
		cell: {
			month: parseInt(cell.dataset.month as string),
			day: parseInt(cell.dataset.day as string),
			personIndex: parseInt(cell.dataset.person as string),
		}
	}

const highlightFromCoordinates = (coordinates: Coordinates, entries: Entry[], people: Person[]): Highlight =>
	(coordinates.entryIndex !== undefined)
		? { entry: entries[coordinates.entryIndex] }
		: {
			cell: {
				...coordinates.cell!,
				person: people[coordinates.cell!.personIndex] as Person
			}
		}

const detectHighlightedEntryClass = (griddedEntry: GriddedEntry, allPeople: string[], highlight: Highlight): string => {
	const coordinates: Coordinates = griddedEntry.coordinates

	if (highlight.cell && coordinates.cell) {
		const hlCell = highlight.cell
		const cell: Cell = coordinates.cell
		if (cell.day === hlCell.day &&
			(cell.month < hlCell.month || (cell.month === hlCell.month && cell.personIndex <= hlCell.personIndex)))
			return style.highlightedRow
		if (cell.month === hlCell.month && cell.personIndex === hlCell.personIndex && cell.day <= hlCell.day)
			return style.highlightedColumn
	}

	if (highlight.entry) {
		if (!coordinates.cell)
			return ""

		const cell = coordinates.cell

		const classes = []
		const entry = highlight.entry
		const peopleIndices = entry.people
			.map(person => person.abbreviation)
			.map(abbreviation => allPeople.indexOf(abbreviation));
		const minPersonIndex = entry.people.length === 0
			? allPeople.length - 1
			: Math.min(...peopleIndices)
		const maxPersonIndex = entry.people.length === 0
			? allPeople.length - 1
			: Math.max(...peopleIndices)

		// highlight row
		const upTo = (_date: DateTime) => cell.day === _date.day &&
			(cell.month < _date.month || (cell.month === _date.month && cell.personIndex <= maxPersonIndex))
		if (upTo(entry.start)) {
			if (entry.length === 1) classes.push(style.highlightedRow)
			else classes.push(style.highlightedTop)
		} else if (upTo(entry.end) && entry.length > 1)
			classes.push(style.highlightedBottom)

		// highlight column
		if (cell.month === entry.start.month && cell.day < entry.start.day) {
			if (entry.people.length === 0) {
				if (cell.personIndex === maxPersonIndex) classes.push(style.highlightedColumn)
			} else if (entry.people.length === 1) {
				if (cell.personIndex === minPersonIndex) classes.push(style.highlightedColumn)
			} else {
				if (cell.personIndex === minPersonIndex) classes.push(style.highlightedLeft)
				if (cell.personIndex === maxPersonIndex) classes.push(style.highlightedRight)
			}
		}

		return classes.join(" ")
	}

	return ""
}

export default Calendar

/*
 * TYPES
 */

interface Highlight {
	// needed because otherwise structurally too similar to `Coordinates`
	__type?: "highlight"
	cell?: CellWithPerson
	entry?: Entry
}

interface CellWithPerson extends Cell {
	person: Person
}
