package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.flightdemo.dtos.FlightDTO;
import dk.cphbusiness.flightdemo.dtos.FlightInfoDTO;
import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {

    public static void main(String[] args) {
        try {
            List<FlightDTO> flightList = getFlightsFromFile("flights.json");
            List<FlightInfoDTO> flightInfoDTOList = getFlightInfoDetails(flightList);
//            flightInfoDTOList.forEach(System.out::println);

            getSpecificAirlineFlightTotal(flightList, "Lufthansa");
            getDepartureArrival(flightList,"Fukuoka","Haneda Airport");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<FlightDTO> getFlightsFromFile(String filename) throws IOException {

        ObjectMapper objectMapper = Utils.getObjectMapper();

        // Deserialize JSON from a file into FlightDTO[]
        FlightDTO[] flightsArray = objectMapper.readValue(Paths.get("flights.json").toFile(), FlightDTO[].class);

        // Convert to a list
        List<FlightDTO> flightsList = List.of(flightsArray);
        return flightsList;
    }

    public static List<FlightInfoDTO> getFlightInfoDetails(List<FlightDTO> flightList) {
        List<FlightInfoDTO> flightInfoList = flightList.stream()
           .map(flight -> {
                LocalDateTime departure = flight.getDeparture().getScheduled();
                LocalDateTime arrival = flight.getArrival().getScheduled();
                Duration duration = Duration.between(departure, arrival);
                FlightInfoDTO flightInfo =
                        FlightInfoDTO.builder()
                            .name(flight.getFlight().getNumber())
                            .iata(flight.getFlight().getIata())
                            .airline(flight.getAirline().getName())
                            .duration(duration)
                            .departure(departure)
                            .arrival(arrival)
                            .origin(flight.getDeparture().getAirport())
                            .destination(flight.getArrival().getAirport())
                            .build();

                return flightInfo;
            })
        .toList();
        return flightInfoList;
    }

    public static List<FlightInfoDTO> getSpecificAirlineFlightTotal(List<FlightDTO> flightList, String airline) {
         List<FlightInfoDTO> flightInfoDTOList =
        flightList.stream()
                .filter(flight -> airline.equalsIgnoreCase(flight.getAirline().getName()))
                .map(flight -> {
                LocalDateTime departure = flight.getDeparture().getScheduled();
                LocalDateTime arrival = flight.getArrival().getScheduled();
                Duration duration = Duration.between(departure, arrival);

                FlightInfoDTO flightInfoDTO = FlightInfoDTO.builder()
                        .name(flight.getFlight().getNumber())
                        .iata(flight.getFlight().getIata())
                        .airline(flight.getAirline().getName())
                        .duration(duration)
                        .departure(departure)
                        .arrival(arrival)
                        .origin(flight.getDeparture().getAirport())
                        .destination(flight.getArrival().getAirport())
                        .build();

                    return flightInfoDTO;
                }).toList();
        System.out.println(flightInfoDTOList.size());
        Duration totalDuration = flightInfoDTOList.stream().map(FlightInfoDTO::getDuration).reduce(Duration.ZERO,Duration::plus);
        System.out.println(totalDuration.toHours() + totalDuration.toMinutes()%60);
        return flightInfoDTOList;
    }


    public static Duration getAverageDurationAirline(List<FlightDTO> flightList, String airline) {
        getSpecificAirlineFlightTotal(flightList, airline);

        List<Duration> durations = flightList.stream().filter(flight -> airline.equalsIgnoreCase(flight.getAirline().getName()))
                .map(flight -> Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled()))
                .toList();

        long totalSeconds = durations.stream()
                .mapToLong(Duration::getSeconds).sum();

        long averageSeconds = totalSeconds / durations.size();

        return Duration.ofSeconds(averageSeconds);

    }

    public static List<FlightDTO> getDepartureArrival (List<FlightDTO> flightList ,String departure, String arrival) {
        List<FlightDTO> flightsList = flightList.stream()
                .filter(flight -> departure.equalsIgnoreCase(flight.getDeparture().getAirport()))
                .filter(flight -> arrival.equalsIgnoreCase(flight.getArrival().getAirport())).collect(Collectors.toList());

        System.out.println(flightsList);
        return flightsList;

    }
    }