import React from 'react'

import style from './window.module.css'

const Window = ({ name, activeTab, className, children }) => {
	const _children = React.Children.toArray(children).filter(Boolean)
	const activeTabIndex = _children.findIndex(tab => tab.props.name === activeTab)
	return (
		<div className={`${style.window} ${className}`}>
			{_children.map((_, index) =>
				<input key={index} id={`${name}-input${index}`} className={style.input} name={`${name}-inputs`} type="radio" checked={index === activeTabIndex} />)
			}
			<div className={style.tabs}>
				{_children.map((tab, index) =>
					<label key={index} id={`${name}-tab${index}`} className={style.tab} htmlFor={`${name}-input${index}`}>
						<span className={style.tabTitle} >{tab.props.name}</span>
					</label>)
				}
			</div>
			<div className={style.panels}>
				{_children.map((tab, index) =>
					<div key={index} id={`${name}-panel${index}`} className={style.panel}>
						{tab}
					</div>)
				}
			</div>
		</div>
	)
}

export default Window
