import { ReadyState } from "react-use-websocket";

import style from './debugInfo.module.css'
import tabStyle from './tab.module.css'

const DebugInfo = ({ layout, triggerNextLayout, theme, triggerNextTheme, command, commandState, triggerNextMessage }) => {
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
					<button className={style.button} onClick={event => triggerNextLayout()}>next</button>
				</li>
				<li>
					<span>theme: {theme}</span>
					<button className={style.button} onClick={event => triggerNextTheme()}>next</button>
				</li>
			</ul>
			<p>Command web socket:</p>
			<ul>
				<li>status: {commandStateString}</li>
				<li>last command: {command?.data || "n.a."}</li>
			</ul>
			<p>Messages: <button className={style.button} onClick={event => triggerNextMessage()}>populate</button></p>
		</div>
	)
}

export default DebugInfo
