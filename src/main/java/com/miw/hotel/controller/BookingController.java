package com.miw.hotel.controller;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.miw.hotel.exceptions.InvalidBookingException;
import com.miw.hotel.exceptions.InvalidRoomException;
import com.miw.hotel.model.Booking;
import com.miw.hotel.model.Client;
import com.miw.hotel.model.Status;
import com.miw.hotel.repository.BookingRepository;
import com.miw.hotel.repository.ClientRepository;
import com.miw.hotel.repository.HotelRepository;
import com.miw.hotel.repository.RoomRepository;
import com.miw.hotel.utils.MailSender;

@RestController
@RequestMapping(value = "/api/books")
public class BookingController {

	@Autowired
	RoomRepository roomRepository;

	@Autowired
	BookingRepository bookRepository;
	
	@Autowired
	ClientRepository clientRepository;
	
	@Autowired
    HotelRepository hotelRepository;
	
	@Autowired
	MailSender mailSender;

	@RequestMapping(value = "", method = RequestMethod.GET)
	public List<Booking> getAllBooks() {
		return bookRepository.findAll();
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Booking getBook(@PathVariable(value = "id") String id) {
		return bookRepository.findById(id);
	}

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	public void cancelBooking(@RequestBody String id) {
		Booking booking = bookRepository.findById(id);
		booking.setStatus(Status.CANCEL.name());
		bookRepository.save(booking);
		
		Client client = clientRepository.findById(booking.getClient().getId());
		String body = "Sr/Sra " + client.getName() + ":\n\nSu reserva en el hotel ";
		body += hotelRepository.findById(roomRepository.findById(booking.getRoom().getId()).getHotel().getId()).getName();
		body += " y con código de reserva ";
		body += booking.getReservationCode();
		body += " ha sido cancelada.\n\nReciba un cordial saludo.";
		mailSender.sendMail(client.getEmail(), "Reserva " + booking.getReservationCode() + " cancelada", body);
	}

	@RequestMapping(method = RequestMethod.POST)
	public void createBook(@RequestBody Booking booking) throws InvalidRoomException, InvalidBookingException {
		
		if (!booking.valid()) {
			throw new InvalidBookingException();
		}

		if (roomRepository.findById(booking.getRoom().getId()) == null) {
			throw new InvalidRoomException();
		}

		if(clientRepository.findByNif(booking.getClient().getNif()) == null) {
			booking.getClient().setId(new ObjectId().toString());
			clientRepository.save(booking.getClient());
		}

		booking.setId(new ObjectId().toString());
		booking.putTotalPriceBook();
		bookRepository.save(booking);

	}
	
	@RequestMapping(value = "/client/{reservationCode}", method = RequestMethod.GET)
    public Booking getBookByReservationCode(@PathVariable String reservationCode){
        Booking booking = bookRepository.findByReservationCode(reservationCode);
        booking.setClient(clientRepository.findById(booking.getClient().getId()));
        booking.setRoom(roomRepository.findById(booking.getRoom().getId()));
        booking.getRoom().setHotel(hotelRepository.findById(booking.getRoom().getHotel().getId()));
        return booking;
    }
	
	@RequestMapping(value = "/price/nif/{nifClient}", method = RequestMethod.GET)
    public double getPriceTotalFromCient(@PathVariable String nifClient) {
	    List<Booking> listadoBooks= null;
	    Client client= clientRepository.findByNif(nifClient);
	    if (client!=null){
	        listadoBooks=bookRepository.findByClient_id(client.getId());
	    }
	    
	    return this.calculateTotalPriceClient(listadoBooks);
    }
	
	private double calculateTotalPriceClient(List<Booking> books){
	    double precioTotalCliente= 0.0;
	    if (books!=null){
	        for(Booking book: books){
	            precioTotalCliente+=book.getTotalPrice();
	        }
	    }
	    return precioTotalCliente;
	}
	
	private boolean newBookTimeIsValid (Booking booking){
	    List<Booking> books= bookRepository.findAll();
	    for (Booking book: books){
	        compareDateBooks(book, booking);
	    }
	    return false;
	}
	
	private boolean compareDateBooks (Booking oldBook, Booking newBook){
	    Date startNewDate= new Date(newBook.getStartDate());
	    Date endNewDate= new Date (newBook.getEndDate());
	    if (isPosibleDate(startNewDate, oldBook)&&(isPosibleDate(endNewDate, oldBook)))
	        return true;
	    return false;
	}
	
	private boolean isPosibleDate(Date date, Booking book){
	    Date startDate= new Date(book.getStartDate());
	    Date endDate= new Date (book.getEndDate());
	    if (date.after(startDate)&&(date.before(endDate)))
	        return false;
	    return true;
	}
}
