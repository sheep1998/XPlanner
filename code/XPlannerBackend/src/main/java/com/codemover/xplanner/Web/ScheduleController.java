package com.codemover.xplanner.Web;

import com.codemover.xplanner.Converter.CompleteRequest;
import com.codemover.xplanner.Converter.UpdateScheduleitmeRequest;
import com.codemover.xplanner.Model.DTO.ScheduleitmeDTO;
import com.codemover.xplanner.Service.ScheduleService;
import com.codemover.xplanner.Web.Util.ControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.text.ParseException;
import java.util.Map;


@RestController
public class ScheduleController {

    @Autowired
    ScheduleService scheduleService;


    @GetMapping(value = "/api/schedules")
    public Map<String, Object> queryAllScheduleitems(Principal principal) {
        String username = principal.getName();
        return ControllerUtil.successHandler(scheduleService.findUserSchedule(username));
    }

    @PostMapping(value = "/api/schedules")
    public Map<String, Object> addScheduleitem(Principal principal,
                                               @RequestBody UpdateScheduleitmeRequest request) {


        String username = principal.getName();
        return ControllerUtil.successHandler(scheduleService.addScheduleItem(request, username));
    }


    @PutMapping(value = "/api/schedules/{id}/complete")
    public Map<String, Object> changeCompleteState(Principal principal,
                                                   @RequestBody CompleteRequest completed,
                                                   @PathVariable Integer id
    ) {

        String username = principal.getName();
        return ControllerUtil.successHandler(scheduleService.changeCompleteState(
                username,
                id,
                completed.completed
        ));

    }

    @PutMapping(value = "/api/schedules/{id}")
    public Map<String, Object> updateScheduleitem(Principal principal,
                                                  @RequestBody UpdateScheduleitmeRequest request,
                                                  @PathVariable Integer id) {
        String username = principal.getName();
        return ControllerUtil.successHandler(scheduleService.updateScheduleItem(id, request, username));
    }

    @DeleteMapping(value = "/api/schedules/{id}")
    public Map<String, Object> deleteScheduleitem(Principal principal,
                                                  @PathVariable Integer id) {
        String username = principal.getName();
        return ControllerUtil.successHandler(scheduleService.deleteScheduleItem(id, username));

    }

    @GetMapping(value = "/api/monthScheduleInfo")
    public Map<String, Object> monthScheduleInfo(Principal principal,
                                                 @RequestParam("year") Integer year,
                                                 @RequestParam("month") Integer month)
            throws ParseException {
        return ControllerUtil.successHandler(scheduleService.getScheduledDays(principal.getName(), year, month));
    }

    @GetMapping(value = "/api/scheduleForDay")
    public Map<String, Object> scheduleFor(Principal principal,
                                           @RequestParam("year") Integer year,
                                           @RequestParam("month") Integer month,
                                           @RequestParam("day") Integer day
    ) {
        return ControllerUtil.successHandler(scheduleService.findSchedule4OneDay(principal.getName(),
                year, month, day));
    }


}
