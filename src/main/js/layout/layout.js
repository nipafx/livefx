import { classNames } from '../components/functions'

import './colors.css'
import './fonts.css'
import style from './layout.module.css'

const Layout = ({ id, className, children }) => {
	const classes = [ style.main ]
	if (className) {
		if (Array.isArray(className)) classes.push(...className)
		else classes.push(className)
	}
	return (
		<main id={id} {...classNames(classes)}>
			{children}
		</main>
	)
}

export default Layout
