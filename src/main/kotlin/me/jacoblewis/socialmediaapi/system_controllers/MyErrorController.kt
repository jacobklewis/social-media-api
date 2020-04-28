package me.jacoblewis.socialmediaapi.system_controllers

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest


@Controller
class MyErrorController : ErrorController {
    override fun getErrorPath(): String = "/error"

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest): Any {
        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)

        if (status == 404) {
            return ModelAndView("redirect:/swagger-ui.html")
        }
        return "error: $status"
    }
}