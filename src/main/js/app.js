import { useEffect, useState } from 'react'
import useWebSocket from 'react-use-websocket'

import Scene from "./scene";
import Tab from "./components/tab";
import DebugInfo from "./components/debugInfo";

const config = {
	"debug": true,
	"stream": "reboot",
}

const LAYOUTS = [ "cam, screen", "screen, large cam", "screen, small cam", "screen, small cam, guest2" ]
const THEMES = [ "green", "red", "orange", "yellow", "blue-light", "blue-dark", "purple", "pink" ]

const App = () => {
	const {
		sendMessage,
		lastJsonMessage: command,
		readyState: commandState
	} = useWebSocket(
		"ws://localhost:8080/command",
		{
			retryOnError: true,
			shouldReconnect: (closeEvent) => true,
			reconnectAttempts: 1_000_000,
			reconnectInterval: 1000,
		})
	const [ layout, setLayout ] = useState(LAYOUTS[0])
	const [ theme, setTheme ] = useState(THEMES[0])
	const [ messages, setMessages ] = useState([])
	const addMessage = newMessage => {
		const newMessages = [newMessage, ...messages];
		newMessages.length = Math.min(newMessages.length, 50)
		setMessages(newMessages)
	}

	useEffect(() => {
		if (command) executeCommand(command, setLayout, setTheme, addMessage)
		const unregisterSceneSetter = registerLayoutSetter(setLayout)
		return () => unregisterSceneSetter()
	}, [ command ])

	const debug = config?.debug
	const guest = config?.guest
	const guest2 = config?.guest2

	return (
		<Scene layout={layout} theme={theme} stream={config.stream} messages={messages}>
			{debug && (
				<Tab name="debug">
					<DebugInfo
						layout={layout} triggerNextLayout={() => triggerNextLayout(layout)}
						theme={theme} triggerNextTheme={() => triggerNextTheme(sendMessage, theme)}
						command={command} commandState={commandState}
						triggerNextMessage={() => triggerNextMessage(sendMessage)}
					/>
				</Tab>)}
		</Scene>
	)
}

const registerLayoutSetter = (setLayout) => {
	const sceneSetter = event => setLayout(event.detail.name)
	window.addEventListener('obsSceneChanged', sceneSetter)
	return () => window.removeEventListener('obsSceneChanged', sceneSetter)
}

const triggerNextLayout = (layout) => {
	const nextLayout = LAYOUTS[(LAYOUTS.indexOf(layout) + 1) % LAYOUTS.length]
	const event = new CustomEvent(
		'obsSceneChanged',
		{ detail: { name: nextLayout } }
	)
	window.dispatchEvent(event)
}


const executeCommand = (command, setLayout, setTheme, addMessage) => {
	console.log("Executing command", command)
	switch (command.type) {
		case "add-chat-message":
			addMessage({
				nick: command.nick,
				text: command.text,
			})
			break
		case "change-theme-color":
			setThemeColor(command.newColor, setTheme)
			break
	}
}

const setThemeColor = (newColor, setTheme) => {
	const themeName = newColor.toLowerCase().replaceAll("_", "-")
	setTheme(themeName)
}

const triggerNextTheme = (sendMessage, theme) => {
	const nextTheme = THEMES[(THEMES.indexOf(theme) + 1) % THEMES.length]
	const nextThemeCommand = {
		type: "change-theme-color",
		newColor: nextTheme,
	}
	sendMessage("ECHO " + JSON.stringify(nextThemeCommand))
}

const triggerNextMessage = (sendMessage) => {
	const nicks = [
		"John Doe",
		"Jane Doe",
	]
	const nickIndex = Math.floor(Math.random() * nicks.length)
	const messages = [
		"<p>This is a simple message.</p>",
		"<p>This is a message with some <b>bold</b>, <i>italic</i>, and <em>emphasized</em> text.</p>",
		"<p>This is a very long message that wraps to the next line, so we can see what that looks like.</p>",
	]
	const messageIndex = Math.floor(Math.random() * messages.length)

	const nextCommand = {
		type: "add-chat-message",
		nick: nicks[nickIndex],
		text: messages[messageIndex],
	}
	sendMessage("ECHO " + JSON.stringify(nextCommand))
}

export default App
