var appConsumer = angular.module('appConsumer', []);

appConsumer.config(function($httpProvider) {
	//Enable cross domain calls
	$httpProvider.defaults.useXDomain = true;
});


appConsumer.controller("userController", function($scope, $http) {

	//Mapa para acceder directamente a las areas de usuario
	const areasUsuario = new Map();
	//Mapa para ver si un area es nueva al borrar, es decir, si aun no se ha guardado en el backend
	const areasNuevas = new Map();
	var copiaDatos;
	$scope.borrar = false;
	$scope.confirmarBorrar = false;
	$scope.cargando = false;
	$scope.password = "";
	$scope.enAcierto = false;
	$scope.enError = false;
	$scope.mensajeError = "";
	$scope.popupAbierto = false;
	$scope.errorObtener = false;

	$scope.getInfo = function() {
		$scope.cargando = true;
		console.log("Consulta lanzada")
		$http.get("/user/info").then(
			function sucessCallback(response) {
				if (response.status == 200) {
					//console.log(response);
					console.log(response.data);
					$scope.usuario = response.data;
					copiaDatos = Object.assign({}, response.data);
					$scope.mydate = new Date(response.data.fnacimiento);
					//Aqui pasamos las areas a un mapa, para acceder directamente al añadir o borrar
					if (response.data.areas.length > 0) {
						for (var i = 0; i < response.data.areas.length; i++) {
							areasUsuario.set(response.data.areas[i].area, response.data.areas[i]);
						}
					}
					else {
						$scope.sinareas = true;
					}
					$scope.areaseleccioanda = "--Escoge una--";
				}
				$scope.cargando = false;
			},
			function errorCallback(response) {
				console.log("Fallo al acceder")
				console.log(response)
				if (response.status == 503) {
					abrirError("Se ha producido un fallo al intentar acceder al repositorio para obtener su información, por favor" +
						"vuelva a intentarlo más tarde");
				}
				else if (response.status == 500) {
					abrirError("Se ha producido un fallo interno en el servidor al intentar obtener su información" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error" +
						", e indique con la mayor precisión el momento en el que este ocurrió.");
				}
				else if (response.status == 403) {
					abrirError("Se ha producido un fallo al intentar acceder a la información de tu cuenta" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error" +
						", e indique con la mayor precisión el momento en el que este ocurrió.");
				}
				else if (response.status == 401) {
					abrirError("No tienes permiso para realizar esta acción.");
				}
				else if (response.status == 0) {
					abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
				}
				else {
					abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las nuevas mentorizaciones" +
						", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error" +
						", e indique con la mayor precisión el momento en el que este ocurrió.");
				}
				$scope.cargando = false;
			}
		)
	}

	$scope.getInfo();

	$scope.setInfo = function(usuario) {
		console.log($scope)
		if ($scope.form.$valid) {
			$scope.cargando = true;
			console.log("Consulta lanzada")
			usuario.fnacimiento = $scope.mydate;
			$http.post("/user/setinfo", usuario).then(
				function sucessCallback(response) {
					//console.log(response);
					console.log(response);
					areasNuevas.clear(); //Borramos las areas para no intentar solo eliminar del frontend un area
					$scope.enAcierto = true;
					$scope.popupAbierto = true;
					$scope.cargando = false;
				},
				function errorCallback(response) {
					console.log("Fallo al acceder")
					console.log(response)
					if (response.status == 503) {
						abrirError("Se ha producido un fallo al intentar acceder al repositorio para actualizar su información, por favor" +
							"vuelva a intentarlo más tarde");
					}
					else if (response.status == 500) {
						abrirError("Se ha producido un fallo interno en el servidor al intentar actualizar su información" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error" +
							", e indique con la mayor precisión el momento en el que este ocurrió.");
					}
					else if (response.status == 400) {
						abrirError("Se ha producido un fallo en la petición al servidor para actualizar tu información" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error" +
							", e indique con la mayor precisión el momento en el que este ocurrió.");
					}
					else if (response.status == 403) {
						abrirError("Se ha producido un fallo al intentar acceder a la información de tu cuenta" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error" +
							", e indique con la mayor precisión el momento en el que este ocurrió.");
					}
					else if (response.status == 401) {
						abrirError("No tienes permiso para realizar esta acción.");
					}
					else if (response.status == 0) {
						abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
					}
					else {
						abrirError("Se ha producido un fallo no previsto con codigo de error " + response.status + " al intentar obtener las nuevas mentorizaciones" +
							", si recibe este error, por favor, pongase en contacto con nosotros y explique en que contexto se generó el error" +
							", e indique con la mayor precisión el momento en el que este ocurrió.");
					}
					$scope.cargando = false;
				}
			)
		}
		else {
			alert("Por favor, introduzca valores válidos en los campos");
		}
	}

	$scope.deshacer = function() {
		console.log(copiaDatos);
		console.log($scope.usuario);
		$scope.usuario = Object.assign({}, copiaDatos);
	}

	//Cambiarlo a add area
	$scope.addArea = function(areaSelecionada) {
		$scope.cargando = true;
		$scope.sinareas = false;
		if (areaSelecionada !== "--Escoge una--") {
			if (areasUsuario.has(areaSelecionada)) {
				abrirError("Ya tienes este área.");
			}
			else {
				$scope.usuario.areas.push({ area: areaSelecionada });
				areasUsuario.set(areaSelecionada, $scope.usuario.areas[$scope.usuario.areas.length - 1]);
				//Esto lo usamos para no tener que llamar a borrar cuando aun no se ha guardado un area
				areasNuevas.set(areaSelecionada, areaSelecionada);
			}
		}
		$scope.cargando = false;
	}


	$scope.borrarArea = function(area) {
		$scope.cargando = true;
		if (areasNuevas.has(area.area)) {
			index = $scope.usuario.areas.indexOf(area);
			console.log(index);
			$scope.usuario.areas.splice(index, 1);
			areasUsuario.delete(area.area);
			areasNuevas.delete(area.area);
			console.log("Borrado solo en el frontend");
			if ($scope.usuario.areas.length == 0) {
				$scope.sinareas = true;
			}
			$scope.cargando = false;
		}
		else {
			$http.post("/user/areas/delete", area).then(
				function sucessCallback(response) {
					if (response.status == 200) {
						console.log(response.data);
						index = $scope.usuario.areas.indexOf(area.area);
						$scope.usuario.areas.splice(index, 1);
						areasUsuario.delete(area.area);
						if ($scope.usuario.areas.length == 0) {
							$scope.sinareas = true;
						}
						$scope.cargando = false;
					}

				},
				function errorCallback(response) {
					console.log("Fallo al eliminar");
					console.log(response);
					if (response.status == 0) {
						abrirError("En estos momentos el servidor se encuentra fuera de servicio, por favor, disculpen las molestias");
					}
					else abrirError(response.data.mensaje);
					$scope.cargando = false;
				}
			)
		}
	}



	$scope.abrirBorrar = function() {
		if ($scope.borrar) {
			$scope.borrar = false
			$scope.popupAbierto = false;
		}
		else {
			$scope.borrar = true;
			$scope.popupAbierto = true;
		}
	}

	$scope.cancelarConfirmar = function() {
		$scope.borrar = false;
		$scope.confirmarBorrar = false;
		$scope.popupAbierto = false;
	}

	$scope.abrirConfirmar = function() {
		if ($scope.password != '') {
			$scope.confirmarBorrar = true;
		}
	}

	var abrirError = function(mensaje) {
		$scope.enError = true;
		$scope.mensajeError = mensaje;
		$scope.popupAbierto = true;
	}

	$scope.cerrarError = function() {
		$scope.enError = false;
		$scope.mensajeError = "";
		$scope.popupAbierto = false;
	}

	$scope.cerrarAcierto = function() {
		$scope.enAcierto = false;
		$scope.popupAbierto = false;
	}

});
