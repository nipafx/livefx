import React from "react"

import { Category, Entry } from "./types"

import EntryDetails from "./entryDetails"
import Legend from "./legend"

const style = require("./sidebar.module.css")

interface SidebarProperties {
	hoveredEntry?: Entry,
	selectedEntry?: Entry,
	categories: Category[]
}

const Sidebar = ({ hoveredEntry, selectedEntry, categories }: SidebarProperties) =>
	<div className={style.sidebar}>
		<EntryDetails key="hovered-details" entry={hoveredEntry}/>
		<EntryDetails key="selected-details" entry={selectedEntry}/>
		<div className={style.spacer}/>
		<Legend key="legend" categories={categories}/>
	</div>

export default Sidebar