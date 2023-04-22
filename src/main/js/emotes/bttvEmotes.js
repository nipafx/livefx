import {useState, useEffect} from 'react';
import React from 'react'
export class BTTVEmote {
    constructor({ id, code }) {
        this.id = id;
        this.code = code;
    }

    get url() {
        return BTTVEmote.url_template
            .replace("{{id}}", this.id)
            .replace("{{scale}}", BTTVEmote.scale_template.x1);
    }

    getUrlForScale(scale) {
        return BTTVEmote.url_template
            .replace("{{id}}", this.id)
            .replace("{{scale}}", BTTVEmote.scale_template[scale]);
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
        return "https://cdn.betterttv.net/emote/{{id}}/{{scale}}";
    }

    static get scale_template() {
        return {
            x1: "1x",
            x2: "2x",
            x3: "3x",
        };
    }
}

export default function BTTVEmotes() {
    const [globalEmotes, setGlobalEmotes] = useState({});
    useEffect(() => {
        async function fetchGlobalEmotes() {
            const res = await fetch("https://api.betterttv.net/3/cached/emotes/global");
            const data = await res.json();
            const emotes = {};
            for (let emote of data) {
                emotes[emote.code] = new BTTVEmote(emote);
            }
            setGlobalEmotes(emotes);
        }
        fetchGlobalEmotes();
    }, []);

    return (
        globalEmotes
    );
}