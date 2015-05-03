var theMessage = function(user, text) {
	return {
		text: text,
		user: user
	};
};

var appState = {
	mainUrl : "/chat",
	token : "TN11EN"
};

var messageList = [];

function run(){
	var appContainer = document.getElementsByClassName("chat")[0];
	appContainer.addEventListener("click", delegateEvent);
	appContainer.addEventListener("keydown", delegateEvent);
	var name = restoreLocal("chat username") || "";
	setName(name);
	if (name != "")
		document.getElementById("changeNameBtn").innerHTML = "Change name";
	else
		$("#enterName").attr("placeholder", "Type your name here");
	restoreHistory();
}

function createMessage(message) {
	if (message.user == document.getElementById("name").innerHTML) {
		message.userFlag = true;
	}
	addMessage(message);
}

function setName(name) {
	document.getElementById("name").innerHTML = name;
}

function delegateEvent(evtObj) {
	if(evtObj.type == "click" &&  evtObj.target.getAttribute("id") == "sendBtn") {
		onSendButtonClick(evtObj);
	}
	if(evtObj.type == "click" && evtObj.target.getAttribute("id") == "changeNameBtn") {
		onChangeNameButtonClick(evtObj);
	}
	if(evtObj.type == "click" && evtObj.target.getAttribute("id") == "iconDelete") {
		onIconDeleteClick(evtObj);
	}
	if(evtObj.type == "click" && evtObj.target.getAttribute("id") == "iconEdit") {
		onIconEditClick(evtObj);
	}
	if(evtObj.type == "keydown" && evtObj.keyCode == 13 && document.activeElement.id == "enterName") {
		onChangeNameButtonClick(evtObj);
	}
	if(evtObj.type == "keydown" && evtObj.shiftKey == false && evtObj.keyCode == 13 && document.activeElement.id == "messageText") {
		evtObj.preventDefault();
		onSendButtonClick(evtObj);
	}
}

function onIconDeleteClick(evtObj) {
	var conf = confirm("Confirm removal, please :)");
	if (conf == true) {
		var message = evtObj.target.parentNode;
		var i;
		for (i = 0; i < messageList.length; i++)
			if (messageList[i].id == message.id)
				break;
		deleteFromServer(messageList[i]);
	}
}

var messageToEdit;

function onIconEditClick(evtObj) {
	var message = evtObj.target.parentNode;
	var messageArea = document.getElementById("messageText");
	messageArea.focus();
	messageArea.value = message.getElementsByTagName("div")[0].getElementsByTagName("span")[1].innerHTML;
	var sendButton = document.getElementById("sendBtn");
	sendBtn.innerHTML = "Edit";
	messageToEdit = message;
}

function onSendButtonClick(){
	if (document.getElementById("name").value == "")
		alert("Enter your name, please :)");
	else if (document.getElementById("messageText").value == "")
		alert("Your message is empty! :(");
	else if (document.getElementById("sendBtn").innerHTML != "Edit") {
		var message = theMessage(document.getElementById("name").value, document.getElementById("messageText").value);
		document.getElementById("messageText").value = "";
		addToServer(message);
	}
	else {
		document.getElementById("sendBtn").innerHTML = "Send";
		var text = document.getElementById("messageText").value;
		document.getElementById("messageText").value = "";
		var i;
		for (i = 0; i < messageList.length; i++)
			if (messageList[i].id == messageToEdit.id)
				break;
		messageList[i].text = text;
		editOnServer(messageList[i]);
	}
}
function onChangeNameButtonClick() {
	var name = document.getElementById("enterName")
	if (name != "")
	{
		document.getElementById("changeNameBtn").innerHTML = "Change name";
		$("#enterName").attr("placeholder", "");
	}
	else
		$("#enterName").attr("placeholder", "Type your name here");
	setName(name.value);
	storeLocal("chat username", name.value);
	name.value = "";
}

function addMessage(message){
	if (message.userFlag == true) {
		var divItem = document.createElement("div");
		var divText = document.createElement("div");
		var spanName = document.createElement("span");
		var spanText = document.createElement("span");
		var deleteImg = document.createElement("img");
		var editImg = document.createElement("img");
		divItem.classList.add("userMessage");
		divItem.id = message.id;
		spanName.classList.add("userName");
		deleteImg.id =  "iconDelete";
		deleteImg.setAttribute("src", "resources/images/delete.png");
		deleteImg.setAttribute("alt", "Delete message");
		editImg.id = "iconEdit";
		editImg.setAttribute("src", "resources/images/edit.png");
		editImg.setAttribute("alt", "Edit message");
		spanName.innerHTML = message.user + ":&nbsp";
		spanText.innerHTML = message.text;
		divText.appendChild(spanName);
		divText.appendChild(spanText);
		divItem.appendChild(divText);
		divItem.appendChild(deleteImg);
		divItem.appendChild(editImg);
	}
	else {
		var divItem = document.createElement("div");
		var spanName = document.createElement("span");
		var spanText = document.createElement("span");
		divItem.classList.add("friendMessage");
		divItem.id = message.id;
		spanName.classList.add("friendName");
		spanName.innerHTML = message.user + ":&nbsp";
		spanText.innerHTML = message.text;
		divItem.appendChild(spanName);
		divItem.appendChild(spanText);
	}
	var items = document.getElementsByClassName("messageArea")[0];
	items.appendChild(divItem);
	messageList.push(message);
	var items = $(".messageArea");
	items.scrollTop(items[0].scrollHeight - items.height());
}

function deleteMessage(message) {
	message.parentNode.removeChild(message);
	var i;
	for (i = 0; i < messageList.length; i++)
		if (messageList[i].id == message.id)
			break;
	var dMessage = messageList[i];
	messageList.splice(i, 1);
	return dMessage;
}

function editMessage(message, text) {
	if (message.className != "friendMessage")
		message.getElementsByTagName("div")[0].getElementsByTagName("span")[1].innerHTML = text;
	else
		message.getElementsByTagName("span")[1].innerHTML = text;
	var i;
	for (i = 0; i < messageList.length; i++)
		if (messageList[i].id == message.id)
			break;
	messageList[i].text = text;
	return messageList[i];
}

function storeLocal(lsName, toSave) {
	if(typeof(Storage) == "undefined") {
		alert("localStorage is not accessible!");
		return;
	}
	localStorage.setItem(lsName, JSON.stringify(toSave));
}

function restoreLocal(lsName) {
	if(typeof(Storage) == "undefined") {
		alert("localStorage is not accessible");
		return;
	}
	var item = localStorage.getItem(lsName);
	return item && JSON.parse(item);
}

function addToServer(message, continueWith) {
	post(appState.mainUrl, JSON.stringify(message), continueWith, function(message) {
		defaultErrorHandler(message);
		wait();
	});
}

function deleteFromServer(message, continueWith) {
	del(appState.mainUrl, JSON.stringify(message), continueWith, function(message) {
		defaultErrorHandler(message);
		wait();
	});
}

function editOnServer(message, continueWith) {
	put(appState.mainUrl, JSON.stringify(message), continueWith, function(message) {
		defaultErrorHandler(message);
		wait();
	});
}

var errorFlag = true;

function restoreHistory(continueWith) {
	var url = appState.mainUrl + '?token=' + appState.token;
	get(url, function(responseText) {
		console.assert(responseText != null);
		var response = JSON.parse(responseText);
		appState.token = response.token;
		var messages = response.messages;
		for (var i = 0; i < messages.length; i++) {
			if (messages[i].actionToDo == "POST") {
				createMessage(messages[i]);
			}
			else if (messages[i].actionToDo == "DELETE") {
				var message = document.getElementById(messages[i].id);
				deleteMessage(message)
			}
			else if (messages[i].actionToDo == "EDIT") {
				var message = document.getElementById(messages[i].id);
				editMessage(message, messages[i].text);
			}
		}
		continueWith && continueWith();
	}, function(message) {
		wait();
	}, function() {
		fatalError();
		wait();
	});  
	setTimeout(function() {
		restoreHistory(continueWith);
	}, 1000);
}

function defaultErrorHandler(message) {
	alert(message);
}

function fatalError() {
	$("#serverOn").css({"color" : "red", "font-size" : "large"});
	$("#serverOn").text("SERVER IS UNAVAILIBLE!");
	 if (errorFlag == true) {
		errorFlag = false;
		appState.token = "TN11EN";
		appState.tokenToEdit = "TK11EN";
		var conf = confirm("Server seems to shut down.\nThe message history is not valid any more :(\nWould you like to delete it?");
		 if (conf)
		 	$(".messageArea").empty();
		messageList = new JSONArray();
	 }
	 
}

function wait() {
	var url = appState.mainUrl + '?token=' + appState.token;
	get(url, function() {
	}, function() {
		setTimeout(function() {
			wait();
		});
	});
}

function get(url, continueWith, continueWithError, fatalError) {
	ajax('GET', url, null, continueWith, continueWithError, fatalError);	
}

function post(url, data, continueWith, continueWithError) {
	ajax('POST', url, data, continueWith, continueWithError);	
}

function put(url, data, continueWith, continueWithError) {
	ajax('PUT', url, data, continueWith, continueWithError);	
}

function del(url, data, continueWith, continueWithError) {
	ajax('DELETE', url, data, continueWith, continueWithError);	
}

function isError(text) {
	if(text == "")
		return false;
	try {
		var obj = JSON.parse(text);
	} catch(ex) {
		return true;
	}
	return !!obj.error;
}

function ajax(method, url, data, continueWith, continueWithError, fatalError) {
	var xhr = new XMLHttpRequest();
	continueWithError = continueWithError || defaultErrorHandler;
	xhr.open(method, url, true);
	xhr.onload = function () {
		if (xhr.readyState !== 4)
			return;

		if(xhr.status != 200) {
			continueWithError('Error on the server side, response ' + xhr.status);
			return;
		}

		if(isError(xhr.responseText)) {
			continueWithError('Error on the server side, response ' + xhr.responseText);
			return;
		}
		$("#serverOn").css({"color" : "RGB(44, 77, 55)", "font-size" : "medium"});
		$("#serverOn").text("Server is availible!");
		if (!errorFlag)
			$(".messageArea").empty();
		errorFlag = true;
		continueWith(xhr.responseText);
	};  
	xhr.ontimeout = function () {
	    continueWithError('Server timed out !');
	}
	xhr.onerror = function () {
	    fatalError();
	};
	xhr.send(data);
}