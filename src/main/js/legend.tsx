import React from 'react'

import { Category } from "./types"

const style = require('./legend.module.css')
const sidebarStyle = require("./sidebar.module.css")

interface LegendProperties {
	categories: Category[]
}

const Legend = ({ categories }: LegendProperties) =>
	<div>
		<div className={sidebarStyle.header}>Legend</div>
		<div className={style.container}>
			{categories.map(displayCategory)}
		</div>
	</div>

const displayCategory = (category: Category) =>
	<div key={category.name} className={style.category} style={{ backgroundColor: category.color }}>
		{category.name}
	</div>

export default Legend
