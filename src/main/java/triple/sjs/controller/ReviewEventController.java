package triple.sjs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import triple.sjs.domain.Points;
import triple.sjs.service.ReviewEventService;

import java.sql.SQLException;
import java.util.List;


@RestController
public class ReviewEventController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ReviewEventService reviewEventService;

    @RequestMapping(value = "/events", method = RequestMethod.POST)
    public ResponseEntity save(HttpServletRequest httpServletRequest, Model model) throws SQLException {
        String type = httpServletRequest.getParameter("type");
        String action = httpServletRequest.getParameter("action");
        String result = "";

        if(type.equals("REVIEW")) {
            if(action.equals("ADD")) {
                result = reviewEventService.reviewAdd(httpServletRequest);
            }else if(action.equals("DELETE")){
                result = reviewEventService.reviewDelete(httpServletRequest);
            }else if(action.equals("MOD")){
                result = reviewEventService.reviewMod(httpServletRequest);
            }

        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/points")
    public ModelAndView list(ModelAndView modelAndView) {
        List<Points> list = reviewEventService.pointList();
        modelAndView.setViewName("list");
        modelAndView.addObject("points", list);

        return modelAndView;
    }


}
