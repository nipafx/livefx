import React from "react";

import EntryDetails from "./entryDetails";
import Legend from "./legend";

import style from "./sidebar.module.css";

const Sidebar = ({ calendar, hoveredEntry, selectedEntry }) =>
	<div className={style.sidebar}>
		<EntryDetails key="hovered-details" {...calendar} entryIndex={hoveredEntry}/>
		<EntryDetails key="selected-details" {...calendar} entryIndex={selectedEntry}/>
		<div className={style.spacer}/>
		<Legend key="legend" {...calendar}/>
	</div>

export default Sidebar