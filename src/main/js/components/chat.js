import style from './chat.module.css'
import tabStyle from './tab.module.css'
import React from 'react'
import BTTVEmotes from "../emotes/bttvEmotes";
import TwitchEmotes from "../emotes/twitchEmotes";
import SevenTVEmotes from "../emotes/SevenTVEmotes";


const Chat = ({messages}) => {
    const bttvGlobalEmotes = BTTVEmotes()
    const twitchGlobalEmotes = TwitchEmotes()
    const sevenTVGlobalEmotes = SevenTVEmotes()
    const globalEmotes = [
        {
            service: "bttv",
            serviceGlobalEmotes: bttvGlobalEmotes},
        {
            service: "twitch",
            serviceGlobalEmotes: twitchGlobalEmotes
        },
        {
            service: "sevenTV",
            serviceGlobalEmotes: sevenTVGlobalEmotes
        }]
    const reorderedMessages = [...messages]
    reorderedMessages.reverse()
    return (
        <div className={tabStyle.content + " " + style.container}>
            <div className={style.messageBox}>
                {messages.map(message => <Message key={messages.indexOf(message)} message={message}
                                                  globalEmotes={globalEmotes}/>)}
            </div>
        </div>
    )
}

const Message = ({message, globalEmotes}) => {
    const wordEmoteMap = parseEmotes(message.text, globalEmotes)
    const msg_words = message.text.split(" ");
    // actually replace everything collected at once
    let parsed_msg = msg_words.map(word => {
        // replace emotes bttv
        if (wordEmoteMap[word]) {
            return Emote(wordEmoteMap[word]);
        }
        return word + " ";
    });
    return (<div className={style.message}>
        <span className={style.nick}>{message.nick}</span>
        <span className={style.text}>{parsed_msg}</span>
    </div>)
}
const parseEmotes = (message_text, globalEmotes) => {
    const wordEmoteMap = {};
    const msg_words = message_text.split(" ");
    // find words to replace with
    msg_words.forEach(str => {
        // global emotes repalcement
        globalEmotes.forEach(service => {
            for (const key in service.serviceGlobalEmotes) {
                if (str === key) {
                    console.log(service.serviceGlobalEmotes[key])
                    wordEmoteMap[key] = {
                        name: key,
                        service: service.service,
                        emote: service.serviceGlobalEmotes[key],
                    };
                }
            }
        });
    });
    return wordEmoteMap;

}

const Emote = (wordEmote) => {
    return (
        <img name={wordEmote.name} src={wordEmote.emote.url} alt={wordEmote.name}/>
    );
}

export default Chat
