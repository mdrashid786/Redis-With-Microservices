package com.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 🔥 API GATEWAY
 *
 * Central entry point for all microservices
 * Responsibilities:
 * ✅ Service routing
 * ✅ Rate limiting
 * ✅ Authentication
 * ✅ Load balancing
 * ✅ Request/Response transformation
 *
 * Port: 8000
 */
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	/**
	 * 🔥 ROUTE CONFIGURATION
	 *
	 * Routes requests to appropriate microservices
	 */
//	@Bean
//	public RouteLocator routeLocator(RouteLocatorBuilder builder) {
//		return builder.routes()
//
//				// ================================================================
//				// PRODUCT SERVICE (8001)
//				// ================================================================
//				.route("product-service",
//						r -> r.path("/api/products/**")
//								.uri("http://localhost:8001"))
//
//				// ================================================================
//				// ORDER SERVICE (8002)
//				// ================================================================
//				.route("order-service",
//						r -> r.path("/api/orders/**")
//								.uri("http://localhost:8002"))
//
//				// ================================================================
//				// PAYMENT SERVICE (8003)
//				// ================================================================
//				.route("payment-service",
//						r -> r.path("/api/payments/**")
//								.uri("http://localhost:8003"))
//
//				// ================================================================
//				// NOTIFICATION SERVICE (8004)
//				// ================================================================
//				.route("notification-service",
//						r -> r.path("/api/notifications/**")
//								.uri("http://localhost:8004"))
//
//				.build();
//	}

	/**
	 * 🔥 WEB CLIENT FOR INTER-SERVICE COMMUNICATION
	 */
//	@Bean
//	public WebClient webClient() {
//		return WebClient.create();
//	}

//	@Bean
//	public RestTemplate restTemplate() {
//		return new RestTemplate();
//	}

}