/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.jms;

import javax.jms.JMSException;

import org.springframework.core.Ordered;
import org.springframework.integration.Message;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.history.MessageHistory;
import org.springframework.integration.history.TrackableComponent;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

/**
 * A MessageConsumer that sends the converted Message payload within a JMS Message.
 * 
 * @author Mark Fisher
 * @author Oleg Zhurakousky
 */
public class JmsSendingMessageHandler extends AbstractJmsTemplateBasedAdapter implements MessageHandler, TrackableComponent, Ordered {

	private volatile int order = Ordered.LOWEST_PRECEDENCE;

	private volatile boolean shouldTrack;


	public JmsSendingMessageHandler(JmsTemplate jmsTemplate) {
		super(jmsTemplate);
	}

	/**
	 * No-arg constructor provided for convenience when configuring with
	 * setters. Note that the initialization callback will validate.
	 */
	public JmsSendingMessageHandler() {
		super();
	}

	public void setShouldTrack(boolean shouldTrack) {
		this.shouldTrack = shouldTrack;
	}

	public String getComponentType() {
		return "jms:outbound-channel-adapter";
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return this.order;
	}

	public final void handleMessage(Message<?> message) {
		if (message == null) {
			throw new IllegalArgumentException("message must not be null");
		}
		final Message<?> messageToSend = (this.shouldTrack) ? MessageHistory.write(message, this) : message;
		Object objectToSend = messageToSend;
		if (this.shouldExtractPayload()) {
			objectToSend = messageToSend.getPayload();
		}
		this.getJmsTemplate().convertAndSend(objectToSend, new MessagePostProcessor() {
			public javax.jms.Message postProcessMessage(javax.jms.Message jmsMessage) throws JMSException {
				getHeaderMapper().fromHeaders(messageToSend.getHeaders(), jmsMessage);
				return jmsMessage;
			}
		});
	}

}