package dev.nipafx.livefx.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LiveFxEndpoints {

	private static final Logger LOG = LoggerFactory.getLogger(LiveFxEndpoints.class);

}
