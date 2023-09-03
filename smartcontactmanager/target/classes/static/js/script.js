//alert("MY Js File")
const toggleSidebar = () => {

	if ($(".sidebar").is(":visible")) {

		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");

	} else {

		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");

	}

};

const search = () => {
	let query = $("#search-input").val();
	if (query == "") {
		$(".search-result").hide();

	} else {

		// console.log(query);

		let url = `http://localhost:/8585/search/${query}`;

		fetch(url)
			.then((response) => {
				return response.json();

			})
			.then((data) => {

				let text = `<div class='list-group'>`;

				data.forEach(contact => {
					text += `<a href='/${contact.cId}/contact' class='list-group-item list-group-action'> ${contact.name}</a>`

				});
				text += `</div>`;

				$(".search-result").html(text);
				$(".search-result").show();

			});
	}

};

//First Request to server to create order

const paymentStart = () => {
	let amount = $("#payment_field").val();

	if (amount == '' || amount == null) {
		Swal.fire("Amount is required!","error");
		retrun;
	}

	//Code for Request order by Jquery-ajax
	$.ajax({
		url: '/create_order',
		data: JSON.stringify({ amount: amount, info: 'order_request' }),
		contentType: 'application/json',
		type: 'POST',
		dataType: 'json',
		success: function(response) {
			//Envoke When Success
			if (response.status == "created") {
				//open payment form
				let options = {
					KeyboardEvent: 'rzp_test_pyPtfg1kQTV9EK',
					amount: response.amount,
					currency: 'INR',
					name: 'Smart Contact Manager',
					description: "Donation",
					image: 'http://localhost:8585/image/scmc.png',
					order_id: response.id,
					handler: function(response) {
						console.log('payment SuccessFul');

						Swal.fire(
							'Good job!',
							'Congrates Payment Success',
							'success'
						)
					},
					"prefill": {
						"name": "Gaurav Kumar",
						"email": "gaurav.kumar@example.com",
						"contact": "9999999999"
					},
					"notes": {
						"address": "Smart Contact Manager",

					},
					"theme": {
						"color": "#3399cc",
					}



				};
				let rzp = new Razorpay(options);
				rzp.on('payment.failed', function(response) {
					Swal.fire("Oops payment failed!","error");
				});
				rzp.open();

			}

		},
		error: function(error) {
			//Invoked When errora
			alert("something wrong");

		}
	}
	)

};
