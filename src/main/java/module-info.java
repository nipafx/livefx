module dev.nipafx.livefx {
	requires spring.boot;
	requires spring.boot.autoconfigure;
	requires spring.beans;
	requires spring.context;
	requires spring.core;
	requires spring.web;

	requires org.slf4j;

	opens dev.nipafx.livefx.spring to spring.beans, spring.core, spring.context;
}
