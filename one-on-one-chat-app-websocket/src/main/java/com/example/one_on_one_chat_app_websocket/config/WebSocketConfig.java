package com.example.one_on_one_chat_app_websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocketメッセージブローカーの設定
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /user プレフィックスを持つ宛先に対するメッセージブローカーを有効化
        registry.enableSimpleBroker("/user", "/topic");

        // /app プレフィックスを持つアプリケーション宛先のメッセージを処理する
        registry.setApplicationDestinationPrefixes("/app");

        registry.setUserDestinationPrefix("/user");
    }

    // STOMPエンドポイントの登録
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // /ws エンドポイントを登録し、SockJSを使用してフォールバックを提供する
        registry.addEndpoint("/ws")
                .withSockJS();
    }

    // メッセージコンバーターの設定
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // デフォルトのコンテンツタイプリゾルバーを設定し、JSONをデフォルトのMIMEタイプとする
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);

        // Jacksonライブラリを使用して、JavaオブジェクトとJSONの間でメッセージを変換するコンバーターを設定
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        converter.setContentTypeResolver(resolver);

        // メッセージコンバーターをリストに追加
        messageConverters.add(converter);
        // デフォルトのメッセージコンバーターを使用するかどうかを返す（falseの場合、追加したコンバーターが使用される）
        return false;
    }

}