package com.kushki.controller;

import java.io.IOException;
import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.kushki.model.ResponseData;
import com.kushki.model.Subscripcion;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

@Controller
public class IntegracionController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Value("${app.private-key}")
	private String privateKey;

	@GetMapping("/home")
	public String home(Model model) {
		return "home";
	}

	@PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String confirm(@ModelAttribute("response") ResponseData response, BindingResult result, ModelMap model)
			throws IOException, UnirestException {
		if (result.hasErrors()) {
			return "error";
		}
		model.addAttribute("response", response);
		model.addAttribute("subscripcion", new Subscripcion());
		return "subscription";
	}

	@PostMapping(value = "/subscription", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String subscription(@ModelAttribute Subscripcion subscripcion, Model model)
			throws IOException, UnirestException {

		HttpResponse<JsonNode> response = Unirest.post("https://api-uat.kushkipagos.com/subscriptions/v1/card")
				.header("private-merchant-id", privateKey).header("content-type", "application/json")
				.body("{\"token\":\"" + subscripcion.getToken_data()
						+ "\",\"planName\":\"Premium\",\"periodicity\":\"monthly\",\"contactDetails\":{\"documentType\":\"CC\",\"documentNumber\":\""
						+ subscripcion.getDocumento() + "\",\"email\":\"test@test.com\",\"firstName\":\""
						+ subscripcion.getNombre() + "\",\"lastName\":\"" + subscripcion.getApellido()
						+ "\",\"phoneNumber\":\"+593988734644\"},\"amount\":{\"subtotalIva\":1,\"subtotalIva0\":0,\"ice\":0,\"iva\":0.14,\"currency\":\"USD\"},\"startDate\":\"2018-09-25\",\"metadata\":{\"plan\":{\"fitness\":{\"cardio\":\"include\",\"rumba\":\"include\",\"pool\":\"include\"}}}}")
				.asJson();

		ResponseData datos = new ResponseData();
		datos.setKushkiToken(subscripcion.getToken_data());
		model.addAttribute("response", datos);
		Subscripcion salida = new Subscripcion();
		salida.setMensaje(response.getBody().toString());
		salida.setTipo("subscripcion");
		model.addAttribute("subscripcion", salida);
		return "subscription";
	}

	@PostMapping(value = "/preAuthorization", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String preAuthorization(@ModelAttribute Subscripcion subscripcion, Model model)
			throws IOException, UnirestException {

		HttpResponse<JsonNode> response = Unirest.post("https://api-uat.kushkipagos.com/card/v1/preAuthorization")
				.header("private-merchant-id", privateKey).header("content-type", "application/json")
				.body("{\"token\":\"" + subscripcion.getToken_data()
						+ "\",\"amount\":{\"subtotalIva\":0,\"subtotalIva0\":600,\"ice\":0,\"iva\":0,\"currency\":\"PEN\"},\"fullResponse\":true}")
				.asJson();

		ResponseData datos = new ResponseData();
		datos.setKushkiToken(subscripcion.getToken_data());
		model.addAttribute("response", datos);
		Subscripcion salida = new Subscripcion();
		salida.setMensaje(response.getBody().toString());
		salida.setTipo("pre-auth");
		model.addAttribute("subscripcion", salida);
		return "subscription";
	}

	
	@PostMapping(value = "/capPreAuthorization", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String capPreAuthorization(@ModelAttribute Subscripcion subscripcion, Model model)
			throws IOException, UnirestException {

		HttpResponse<JsonNode> response = Unirest.post("https://api-uat.kushkipagos.com/card/v1/capture")
				.header("private-merchant-id", privateKey)
				.header("content-type", "application/json")
				.body("{\"ticketNumber\":\"" + subscripcion.getNumTicket()
						+ "\",\"amount\":{\"currency\":\"PEN\",\"subtotalIva\":0,\"iva\":0,\"subtotalIva0\":600,\"ice\":0},\"fullResponse\":true}")
				.asJson();

		ResponseData datos = new ResponseData();
		datos.setKushkiToken(subscripcion.getToken_data());
		model.addAttribute("response", datos);
		Subscripcion salida = new Subscripcion();
		salida.setMensaje(response.getBody().toString());
		salida.setTipo("cap-pre-auth");
		model.addAttribute("subscripcion", salida);
		return "subscription";
	}

	@PostMapping(value = "/getSubscription", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String getSubscription(@ModelAttribute Subscripcion subscripcion, Model model)
			throws IOException, UnirestException {

		HttpResponse<JsonNode> response = Unirest
				.get("https://api-uat.kushkipagos.com/subscriptions/v1/card/search/" + subscripcion.getIdSubscripcion())
				.header("private-merchant-id", privateKey).asJson();

		ResponseData datos = new ResponseData();
		datos.setKushkiToken(subscripcion.getToken_data());
		model.addAttribute("response", datos);
		Subscripcion salida = new Subscripcion();
		salida.setMensaje(response.getBody().toString());
		salida.setTipo("get-subscription");
		model.addAttribute("subscripcion", salida);
		return "subscription";
	}
	
	@PostMapping(value = "/voidTransaction", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String voidTransaction(@ModelAttribute Subscripcion subscripcion, Model model)
			throws IOException, UnirestException {

		HttpResponse<JsonNode> response = Unirest
				.delete("https://api-uat.kushkipagos.com/v1/charges/" + subscripcion.getNumTicket())
				.header("private-merchant-id", privateKey)	
				.body("{\"fullResponse\": true}")				
				.asJson();

		ResponseData datos = new ResponseData();
		datos.setKushkiToken(subscripcion.getToken_data());
		model.addAttribute("response", datos);
		Subscripcion salida = new Subscripcion();
		salida.setMensaje(response.getBody().toString());
		salida.setTipo("void-subscription");
		model.addAttribute("subscripcion", salida);
		return "subscription";
	}
	
	@PostMapping(value = "/getTransactionList", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String getTransactionList(@ModelAttribute Subscripcion subscripcion, Model model)
			throws IOException, UnirestException {

		HttpResponse<JsonNode> response = Unirest
				  .get("https://api-uat.kushkipagos.com/analytics/v1/transactions-list?" + subscripcion.getParametro())
				  .header("private-merchant-id", privateKey)
				  .asJson();

		ResponseData datos = new ResponseData();
		datos.setKushkiToken(subscripcion.getToken_data());
		model.addAttribute("response", datos);
		Subscripcion salida = new Subscripcion();
		salida.setMensaje(response.getBody().toString());
		salida.setTipo("get-trx-list");
		model.addAttribute("subscripcion", salida);
		return "subscription";
	}
}