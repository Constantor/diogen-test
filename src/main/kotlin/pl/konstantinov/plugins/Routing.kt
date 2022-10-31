package pl.konstantinov.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.title
import org.ahocorasick.trie.*
import java.io.File
import kotlinx.html.*


fun Application.configureRouting() {
	routing {
		get("/") {
			//call.respondText("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Diogen test: words searcher</title></head><body>The app searches all the files that contain each of the words you listed using Aho-Corasick. Type each word in the separate line in the textarea below and then click on the \"Search\" button.<br><form action=\"/search\" method=\"GET\"><textarea></textarea></form></body></html>")
			call.respondHtml(HttpStatusCode.OK) {
				head {
					meta(charset = "utf-8")
					meta(name = "viewport", content = "width=device-width, initial-scale=1, shrink-to-fit=no")
					title {
						+"Diogen test: words searcher"
					}
				}
				body {
					p {
						+"The app searches all the files that contain each of the words you listed using Aho-Corasick. Type searched words in the input below separated with \";\" and then click on the \"Search\" button. (everything is case-sensitive)"
					}
					form(action = "/search", encType = FormEncType.applicationXWwwFormUrlEncoded, method = FormMethod.get) {
						attributes["target"] = "_blank"
						p {
							textInput(name = "words") {
								attributes["size"] = "64"
							}
						}
						p {
							submitInput() { value = "Search" }
						}
					}
				}
			}
		}

		get("/search") {
			val searchDir: String = System.getenv("SEARCH_DIR") ?: return@get call.respondText("Dir env variable wasn't set properly at all.", status = HttpStatusCode.InternalServerError)
			val directory = File(searchDir)
			if(!directory.isDirectory)
				return@get call.respondText("Dir path is incorrect or doesn't exist.", status = HttpStatusCode.InternalServerError)
			//println("Vot: ${(call.request.queryParameters["words"] ?: "").toCharArray().joinToString(";")}}")
			val searchedWords = (call.request.queryParameters["words"] ?: "").split(";")
			//val searchedWords = (call.receiveParameters()["words"] ?: "").split("\n")
			//println(searchedWords.joinToString())
			//println()
			val searchedQuantity = searchedWords.size
			val trie: Trie = Trie.builder().addKeywords(searchedWords).build()
			val responseTextBuilder = StringBuilder().append("The files each of which contains all the words you listed:\n")
			directory.walk().forEach { file ->
				if(!file.isDirectory) {
					// val emits: Collection<Emit> = trie.parseText(file.readText())
					if(trie.parseText(file.readText()).size == searchedQuantity)
						responseTextBuilder.append("${file.name}\n")
				}
			}
			call.respondText(responseTextBuilder.toString())
		}
	}
}
