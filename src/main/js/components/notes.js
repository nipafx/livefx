import tabStyle from './tab.module.css'

const Notes = ({ topic }) => (
	<div className={tabStyle.content} dangerouslySetInnerHTML={{__html: topic}} />
)

export default Notes
