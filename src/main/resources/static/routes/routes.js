/**
 * CommonJS file for application routing. For each URI, angularJS loads a
 * different template with the associated CSS stylesheet.
 */

angular.module("reservas").config(function($routeProvider) {
	"use strict";

	$routeProvider.when('/', {
		templateUrl : "templates/home.html",
		css: "css/home.css"
	})
	
	.when('/books', {
		templateUrl : "templates/books.html",
		controller: "BooksController",
		css: "css/books.css"
	})
	
	.when('/book_room', {
		templateUrl : "templates/hotel_list_to_book.html",
		controller: "BookController",
		controllerAs: "vm",
		css: "css/hotel_list_to_book.css"
	})
	
	.when('/cancel_book', {
		templateUrl : "templates/cancel_book.html",
		controller: "CancelController",
		controllerAs: "vm"
	})
		
	.when('/error', {
		templateUrl : "templates/error/404.html"
	})
	
	.otherwise({
		redirectTo : '/'
	});
});