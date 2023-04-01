import style from './tab.module.css'

// `name` is needed for the surrounding window to determine the tab tile
const Tab = ({ name, className, children }) => (
	<div className={`${style.tab} ${className}`}>
		{children}
	</div>
)

export default Tab
