import {useState, useEffect} from 'react';

export class SevenTVEmote {
    constructor({id, code}) {
        this.id = id;
        this.code = code;
    }

    get url() {
        return SevenTVEmote.url_template
            .replace("{{id}}", this.id)
            .replace("{{scale}}", SevenTVEmote.scale_template.x1);
    }

    getUrlForScale(scale) {
        return SevenTVEmote.url_template
            .replace("{{id}}", this.id)
            .replace("{{scale}}", SevenTVEmote.scale_template[scale]);
    }

    get x1Url() {
        return this.getUrlForScale("x1");
    }

    get x2Url() {
        return this.getUrlForScale("x2");
    }

    get x3Url() {
        return this.getUrlForScale("x3");
    }

    static get url_template() {
        return "https://cdn.7tv.app/emote/{{id}}/{{scale}}";
    }

    static get scale_template() {
        return {
            x1: "1x.webp",
            x2: "2x.webp",
            x3: "3x.webp",
        };
    }
}

export default function SevenTVEmotes() {
    const [globalEmotes, setGlobalEmotes] = useState({});
    useEffect(() => {
        async function fetchGlobalEmotes() {
            const res = await fetch("https://api.7tv.app/v2/emotes/global");
            const data = await res.json();
            const emotes = {};
            for (let emote of data) {
                emotes[emote.name] = new SevenTVEmote(emote);
            }
            setGlobalEmotes(emotes);
        }

        fetchGlobalEmotes();
    }, []);

    return (
        globalEmotes
    );
}