package com.tfg.mentoring.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.mentoring.service.ActiveUsersService;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

	
	@Autowired
	private ActiveUsersService acservice;
	
	@Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker( "/usuario");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/usuario");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/websocket")
                .withSockJS();
    }
    
    //Esto sirve para convertir los mensajes a y de JSON
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        converter.setContentTypeResolver(resolver);
        messageConverters.add(converter);
        return false;
    }
    
    //https://stackoverflow.com/questions/54330744/spring-boot-websocket-how-to-get-notified-on-client-subscriptions
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration){
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                //System.out.println(accessor.toString());
                if(StompCommand.CONNECT.equals(accessor.getCommand())){
                    //System.out.println("Connect ");
                    //System.out.println(accessor.getUser().getName());
                    acservice.entrarNotificacion(accessor.getUser().getName());
                } 
                else if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())){
                    //System.out.println("Subscribe ");
                } 
                else if(StompCommand.SEND.equals(accessor.getCommand())){
                    //System.out.println("Send message ");
                } 
                else if(StompCommand.DISCONNECT.equals(accessor.getCommand())){
                    //System.out.println("Exit ");
                    acservice.salirNotificacion(accessor.getUser().getName());
                } 
                else {
                }
                return message;
            }
        });
    }
	
}
