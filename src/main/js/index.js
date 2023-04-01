import React from 'react'
import * as ReactDOMClient from 'react-dom/client';

import Home from './home'

const container = document.getElementById('root');
const root = ReactDOMClient.createRoot(container);
root.render(
	<React.StrictMode>
		<Home />
	</React.StrictMode>
)
