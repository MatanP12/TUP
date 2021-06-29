package engine.planTrip;

import common.DayOpeningHours;
import engine.attraction.Attraction;
import javafx.util.converter.LocalDateStringConverter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Scanner;

public class DayPlan {
    int MAX_DURATION = 10;
    ArrayList<OnePlan> daySchedule;
    LocalDate date;
    Attraction hotel;
    int durationDay = 0;

    public DayPlan(ArrayList<OnePlan> daySchedule, LocalDate date) {
        this.daySchedule = daySchedule;
        this.date = date;
    }

    public DayPlan(Attraction hotel,LocalDate date) {
        this.hotel = hotel;
        this.date = date;
    }

    public void calculateDayPlan(ArrayList<Attraction> possibleAttractions) {
        Attraction currentAttraction = hotel;
        Attraction nextAttraction;
        LocalTime currentTime = LocalTime.parse("10:00" ,DateTimeFormatter.ofPattern("HH:mm"));
        while (durationDay <= MAX_DURATION) {
            nextAttraction = chooseBestNextAttraction(currentAttraction, currentTime,possibleAttractions);
            daySchedule.add(new OnePlan(nextAttraction,currentTime));
            possibleAttractions.remove(nextAttraction);
            currentAttraction = nextAttraction;
            currentTime = currentTime.plusHours(nextAttraction.getDuration());
            durationDay = nextAttraction.getDuration();

        }

    }

    private Attraction chooseBestNextAttraction(Attraction currentAttraction, LocalTime time, ArrayList<Attraction> possibleAttractions){
        Attraction nextAttraction = null;
        double minScore = Integer.MAX_VALUE;
        double currentScore;
        for(Attraction attraction : possibleAttractions){
            currentScore = calculateScore(currentAttraction,attraction,time,this.date);
            if (currentScore < minScore){
                nextAttraction = attraction;
                minScore = currentScore;
            }
        }
        if (nextAttraction == null)
            nextAttraction = hotel; //the day is over
        return nextAttraction;
    }


    public double calculateScore(Attraction currentAttraction,Attraction nextAttraction, LocalTime hourOnClock, LocalDate date) {
        double scoreDistance = currentAttraction.calcDistanceBetweenAttractions(nextAttraction);
        long differenceBetweenClockAndStartTime;
        long minValue = Integer.MAX_VALUE;
        double scoreTime;
        Boolean closeAttraction;  //in case the next attraction is close on the hourOnClock - cant go to close attraction
        Boolean overPossibleDuration;  //in case according to duration of attraction we'll stay longer then we can

        DayOpeningHours dayOpeningHoursNext = nextAttraction.getOpeningHoursByDay(date.getDayOfWeek());
        ArrayList<LocalTime> openingHoursNext = new ArrayList<>(dayOpeningHoursNext.getOpeningHoursLocalTime());
        ArrayList<LocalTime> closingHoursNext = new ArrayList<>(dayOpeningHoursNext.getClosingHoursLocalTime());
        int sizeHoursNext = openingHoursNext.size();
        LocalTime hourOnClockAfterEnjoying = hourOnClock.plusHours(nextAttraction.getDuration());

        for (int i = 0; i < sizeHoursNext; i++) {
            closeAttraction = hourOnClock.isAfter(openingHoursNext.get(i));
            overPossibleDuration = closingHoursNext.get(i).isBefore(hourOnClockAfterEnjoying);
            if (closeAttraction || overPossibleDuration) {
                openingHoursNext.remove(openingHoursNext.get(i));
                closingHoursNext.remove(closingHoursNext.get(i));
            }
        }
        if (openingHoursNext.size() == 0)
            return Integer.MAX_VALUE;


        // the hourClock = 10:00
        //the thought was that hourClock = 10:00 and if there are att1 = 7:00  and att2 = 8:00 then Chances are that att1 will close earlier so we should choose her
        // that's why i give her the -1*minValue

        //why minValue?
        // in case there are for one attraction 7:00-14:00 and 15:00-19:00
        //  we want to choose 7:00

        for (int i = 0; i < sizeHoursNext; i++) {
            differenceBetweenClockAndStartTime = openingHoursNext.get(i).until(hourOnClock, ChronoUnit.HOURS);
            if (minValue > differenceBetweenClockAndStartTime)
                minValue = differenceBetweenClockAndStartTime;
        }
        scoreTime = -1 * minValue;


        return scoreDistance + scoreTime;
    }
}