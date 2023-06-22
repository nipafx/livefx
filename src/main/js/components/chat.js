import style from './chat.module.css'
import tabStyle from './tab.module.css'

const Chat = ({ messages, graphics }) => {
	return (
		<div className={tabStyle.content + " " + style.container}>
			<div className={style.messageBox}>
				{messages.map(message => <Message key={message.id} message={message} graphics={graphics} />)}
			</div>
		</div>
	)
}

const Message = ({ message, graphics }) => {
	console.log(message.id)
	return (
		<div className={style.message}>
			<Badge msgId={message.id} msgBadges={message.badges} badges={graphics.badges} />
			<div className={style.nick}>{message.nick}</div>
			<div className={style.text}>
				{message.blocks.map((block, index) => <Block key={index} block={block} />)}
			</div>
		</div>
	)
}

const Badge = ({ msgId, msgBadges, badges }) => {
	if (!msgBadges || !badges) return null

	const existingBadges = msgBadges
		.filter(badge => badges[badge])
		.map(badge => badges[badge].url)

	if (existingBadges.length === 0)
		return null

	// this index is pseudo-random but (importantly!) stable per message,
	// so for every message, the "random" badge will always be the same
	const index = parseInt(msgId.split("-")[0], 16) % existingBadges.length
	return <img className={style.badge} src={existingBadges[index]} />
}

const Block = ({ block }) => {
	switch (block.type) {
		case "paragraph":
			return <p dangerouslySetInnerHTML={{ __html: block.text }} />
		case "code":
			return <pre><code>{block.text}</code></pre>
		default:
			return null
	}
}

export default Chat
