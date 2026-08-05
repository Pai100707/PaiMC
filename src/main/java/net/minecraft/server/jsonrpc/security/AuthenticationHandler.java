package net.minecraft.server.jsonrpc.security;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import org.slf4j.Logger;

@Sharable
public class AuthenticationHandler extends ChannelDuplexHandler {
   private final Logger LOGGER = LogUtils.getLogger();
   private static final AttributeKey<Boolean> AUTHENTICATED_KEY = AttributeKey.valueOf("authenticated");
   private static final AttributeKey<Boolean> ATTR_WEBSOCKET_ALLOWED = AttributeKey.valueOf("websocket_auth_allowed");
   private static final String SUBPROTOCOL_VALUE = "minecraft-v1";
   private static final String SUBPROTOCOL_HEADER_PREFIX = "minecraft-v1,";
   public static final String BEARER_PREFIX = "Bearer ";
   private final SecurityConfig securityConfig;
   private final Set<String> allowedOrigins;

   public AuthenticationHandler(SecurityConfig $$0, String $$1) {
      this.securityConfig = $$0;
      this.allowedOrigins = Sets.newHashSet($$1.split(","));
   }

   public void channelRead(ChannelHandlerContext $$0, Object $$1) throws Exception {
      String $$2 = this.getClientIp($$0);
      if ($$1 instanceof HttpRequest $$3) {
         AuthenticationHandler.SecurityCheckResult $$4 = this.performSecurityChecks($$3);
         if (!$$4.isAllowed()) {
            this.LOGGER.debug("Authentication rejected for connection with ip {}: {}", $$2, $$4.getReason());
            $$0.channel().attr(AUTHENTICATED_KEY).set(false);
            this.sendUnauthorizedResponse($$0, $$4.getReason());
            return;
         }

         $$0.channel().attr(AUTHENTICATED_KEY).set(true);
         if ($$4.isTokenSentInSecWebsocketProtocol()) {
            $$0.channel().attr(ATTR_WEBSOCKET_ALLOWED).set(Boolean.TRUE);
         }
      }

      Boolean $$5 = (Boolean)$$0.channel().attr(AUTHENTICATED_KEY).get();
      if (Boolean.TRUE.equals($$5)) {
         super.channelRead($$0, $$1);
      } else {
         this.LOGGER.debug("Dropping unauthenticated connection with ip {}", $$2);
         $$0.close();
      }
   }

   public void write(ChannelHandlerContext $$0, Object $$1, ChannelPromise $$2) throws Exception {
      if ($$1 instanceof HttpResponse $$3
         && $$3.status().code() == HttpResponseStatus.SWITCHING_PROTOCOLS.code()
         && $$0.channel().attr(ATTR_WEBSOCKET_ALLOWED).get() != null
         && ((Boolean)$$0.channel().attr(ATTR_WEBSOCKET_ALLOWED).get()).equals(Boolean.TRUE)) {
         $$3.headers().set(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL, "minecraft-v1");
      }

      super.write($$0, $$1, $$2);
   }

   private AuthenticationHandler.SecurityCheckResult performSecurityChecks(HttpRequest $$0) {
      String $$1 = this.parseTokenInAuthorizationHeader($$0);
      if ($$1 != null) {
         return this.isValidApiKey($$1)
            ? AuthenticationHandler.SecurityCheckResult.allowed()
            : AuthenticationHandler.SecurityCheckResult.denied("Invalid API key");
      } else {
         String $$2 = this.parseTokenInSecWebsocketProtocolHeader($$0);
         if ($$2 != null) {
            if (!this.isAllowedOriginHeader($$0)) {
               return AuthenticationHandler.SecurityCheckResult.denied("Origin Not Allowed");
            } else {
               return this.isValidApiKey($$2)
                  ? AuthenticationHandler.SecurityCheckResult.allowed(true)
                  : AuthenticationHandler.SecurityCheckResult.denied("Invalid API key");
            }
         } else {
            return AuthenticationHandler.SecurityCheckResult.denied("Missing API key");
         }
      }
   }

   private boolean isAllowedOriginHeader(HttpRequest $$0) {
      String $$1 = $$0.headers().get(HttpHeaderNames.ORIGIN);
      return $$1 != null && !$$1.isEmpty() ? this.allowedOrigins.contains($$1) : false;
   }

   
   private String parseTokenInAuthorizationHeader(HttpRequest $$0) {
      String $$1 = $$0.headers().get(HttpHeaderNames.AUTHORIZATION);
      return $$1 != null && $$1.startsWith("Bearer ") ? $$1.substring("Bearer ".length()).trim() : null;
   }

   
   private String parseTokenInSecWebsocketProtocolHeader(HttpRequest $$0) {
      String $$1 = $$0.headers().get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
      return $$1 != null && $$1.startsWith("minecraft-v1,") ? $$1.substring("minecraft-v1,".length()).trim() : null;
   }

   public boolean isValidApiKey(String $$0) {
      if ($$0.isEmpty()) {
         return false;
      } else {
         byte[] $$1 = $$0.getBytes(StandardCharsets.UTF_8);
         byte[] $$2 = this.securityConfig.secretKey().getBytes(StandardCharsets.UTF_8);
         return MessageDigest.isEqual($$1, $$2);
      }
   }

   private String getClientIp(ChannelHandlerContext $$0) {
      InetSocketAddress $$1 = (InetSocketAddress)$$0.channel().remoteAddress();
      return $$1.getAddress().getHostAddress();
   }

   private void sendUnauthorizedResponse(ChannelHandlerContext $$0, String $$1) {
      String $$2 = "{\"error\":\"Unauthorized\",\"message\":\"" + $$1 + "\"}";
      byte[] $$3 = $$2.getBytes(StandardCharsets.UTF_8);
      DefaultFullHttpResponse $$4 = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED, Unpooled.wrappedBuffer($$3));
      $$4.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
      $$4.headers().set(HttpHeaderNames.CONTENT_LENGTH, $$3.length);
      $$4.headers().set(HttpHeaderNames.CONNECTION, "close");
      $$0.writeAndFlush($$4).addListener($$1x -> $$0.close());
   }

   static class SecurityCheckResult {
      private final boolean allowed;
      private final String reason;
      private final boolean tokenSentInSecWebsocketProtocol;

      private SecurityCheckResult(boolean $$0, String $$1, boolean $$2) {
         this.allowed = $$0;
         this.reason = $$1;
         this.tokenSentInSecWebsocketProtocol = $$2;
      }

      public static AuthenticationHandler.SecurityCheckResult allowed() {
         return new AuthenticationHandler.SecurityCheckResult(true, null, false);
      }

      public static AuthenticationHandler.SecurityCheckResult allowed(boolean $$0) {
         return new AuthenticationHandler.SecurityCheckResult(true, null, $$0);
      }

      public static AuthenticationHandler.SecurityCheckResult denied(String $$0) {
         return new AuthenticationHandler.SecurityCheckResult(false, $$0, false);
      }

      public boolean isAllowed() {
         return this.allowed;
      }

      public String getReason() {
         return this.reason;
      }

      public boolean isTokenSentInSecWebsocketProtocol() {
         return this.tokenSentInSecWebsocketProtocol;
      }
   }
}
