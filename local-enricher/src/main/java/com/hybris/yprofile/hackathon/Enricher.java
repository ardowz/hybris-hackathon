/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 */
package com.hybris.yprofile.hackathon;

import com.hybris.yprofile.graph.Node;
import com.hybris.yprofile.pubsub.PubSubService;
import com.hybris.yprofile.securegraph.GraphServiceJSWrapper;
import com.hybris.yprofile.securegraph.SecureGraphService;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.ImmutableMap;


/**
 * Listens for storefront events and creates subgraph: Session ---> HelloWorld
 */
public class Enricher
{
	public static SecureGraphService secureGraphService;

	private static Logger LOG = LoggerFactory.getLogger(Enricher.class);

	public static void main(final String[] beast) {
		ApplicationContext context =
				new ClassPathXmlApplicationContext("META-INF/application-spring.xml");

		secureGraphService = (SecureGraphService) context.getBean("graphService");
		final PubSubService pubSubClient = (PubSubService) context.getBean("pubSubService");
		Map<String, Object> event = null;

		while ((event = pubSubClient.readMessage()) != null) {
			LOG.info("Received event: " + event);

			Node sessionNode = secureGraphService.createNode(
					new GraphServiceJSWrapper(secureGraphService,
							(String) event.get("tenant"),
							(String) event.get("consent-reference")),
					"commerce/Session", (String) event.get("sessionId"), ImmutableMap.of());

			Node helloWorldNode = secureGraphService.createNode(
					new GraphServiceJSWrapper(secureGraphService,
							(String) event.get("tenant"),
							(String) event.get("consent-reference")),
					"hackathon/HelloWorld", "1", ImmutableMap.of());

			sessionNode.createRelation(helloWorldNode, "hackathon/HAS", "1", ImmutableMap.of());
			LOG.info("Relation created: Session[" + sessionNode.getId() +
					"] ---> HelloWorld[" + helloWorldNode.getId() + "]");
		}
	}

}
