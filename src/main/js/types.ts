import { DateTime, Interval } from "luxon";

/*
 * GENERAL
 */

export interface Category {
	name: string
	color: string
}

export interface Entry {
	category: Category
	description: string
	people: Person[]

	start: DateTime
	end: DateTime
	length: number
}

export interface Holiday {
	name: string
	date: DateTime
}

export interface Person {
	name: string
	abbreviation: string
}

export interface Theme {
	name: string,
	textColor: string,
	cellColor: string,
}

export interface ThemedYear {
	/** 12 themes (one per month) */
	themes: Theme[],
}

/*
 * CALENDAR STRUCTURE
 */

export interface CalendarStructure {
	griddedEntries: GriddedEntry[]
	gridStyle: GridStyle
	months: Month[]
	people: Person[]
}

export interface GriddedEntry {
	coordinates: Coordinates
	reactKey: string
	gridArea: GridArea
	className: string
	color?: string
}

export interface GridStyle {
	gridTemplateRows: string
	gridTemplateColumns: string
	gridTemplateAreas: string
}

export interface GridArea {
	gridColumn: string
	gridRow: string
}

export interface Coordinates {
	cell?: Cell
	entryIndex?: number
}

export interface Cell {
	month: number
	day: number
	personIndex: number
}

export interface Month {
	name: string
	abbreviation: string
	number: number
}
