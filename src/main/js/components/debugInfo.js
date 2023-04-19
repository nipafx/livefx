import { ReadyState } from "react-use-websocket";

import style from './debugInfo.module.css'
import tabStyle from './tab.module.css'

const DebugInfo = ({ layout, nextLayout, theme, nextTheme, command, commandState }) => {
	const commandStateString = {
		[ReadyState.CONNECTING]: 'connecting...',
		[ReadyState.OPEN]: 'open',
		[ReadyState.CLOSING]: 'closing...',
		[ReadyState.CLOSED]: 'closed',
		[ReadyState.UNINSTANTIATED]: 'uninstantiated',
	}[commandState]
	return (
		<div className={tabStyle.content}>
			<p>Visuals:</p>
			<ul>
				<li>
					<span>layout: {layout}</span>
					<NextLayoutButton nextLayout={nextLayout} /></li>
				<li>theme: {theme}</li>
			</ul>
			<p>Command web socket:</p>
			<ul>
				<li>status: {commandStateString}</li>
				<li>last command: {command?.data || "n.a."}</li>
			</ul>
		</div>
	)
}

const NextLayoutButton = ({ nextLayout }) => (
	<button className={style.button} onClick={event => triggerNextScene(nextLayout)}>
		next
	</button>
)

const triggerNextScene = nextLayout => {
	const event = new CustomEvent(
		'obsSceneChanged',
		{
			detail: {
				name: nextLayout()
			}
		})
	window.dispatchEvent(event)
}

export default DebugInfo
