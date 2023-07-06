import style from './chat.module.css'
import tabStyle from './tab.module.css'

const Chat = ({ messages }) => {
	return (
		<div className={tabStyle.content + " " + style.container}>
			<div className={style.messageBox}>
				{messages.map(message => <Message key={message.id} message={message} />)}
			</div>
		</div>
	)
}

const Message = ({ message }) => {
	return (
		<div className={style.message}>
			<Badge msgId={message.id} msgBadges={message.badges} />
			<div className={style.nick}>{message.nick}</div>
			<div className={style.text}>
				{message.blocks.map((block, index) => <Block key={index} block={block} />)}
			</div>
		</div>
	)
}

const Badge = ({ msgId, msgBadges }) => {
	if (!msgBadges) return null

	// this index is pseudo-random but (importantly!) stable per message,
	// so for every message, the "random" badge will always be the same
	const index = parseInt(msgId.split("-")[0], 16) % msgBadges.length
	return <img className={style.badge} src={msgBadges[index]} />
}

const Block = ({ block }) => {
	switch (block.type) {
		case "paragraph":
			return <p>{block.elements.map((element, index) => <Inline key={index} inline={element} />)}</p>
		case "code":
			return <pre><code>{block.text}</code></pre>
		default:
			return null
	}
}

const Inline = ({ inline }) => {
	switch (inline.type) {
		case "text":
			return <span dangerouslySetInnerHTML={{ __html: inline.text }} />
		case "emote":
			return <img className={style.emote} src={inline.url} />
		default:
			return null
	}
}

export default Chat
