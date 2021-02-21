import React from 'react'

import style from './legend.module.css'
import sidebarStyle from "./sidebar.module.css";

const Legend = ({ categories }) =>
	<div>
		<div className={sidebarStyle.header}>Legend</div>
		<div className={style.container}>

			{categories.map(displayCategory)}
		</div>
	</div>

const displayCategory = category =>
	<div className={style.category} style={{ backgroundColor: category.color }}>
		{category.name}
	</div>

export default Legend
