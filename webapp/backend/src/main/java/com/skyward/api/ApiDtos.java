package com.skyward.api;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
public final class ApiDtos {
 private ApiDtos(){}
 public record Register(@NotBlank @Size(max=120) String name,@NotBlank @Email @Size(max=254) String email,@Size(max=32) String phone,@NotBlank @Size(min=8,max=100) String password){}
 public record AdminRegister(@NotBlank @Size(max=80) String username,@NotBlank @Size(min=8,max=100) String password,@NotBlank String adminPasskey){}
 public record Login(@NotBlank String login,@NotBlank String password,String adminPasskey){}
 public record LoginResult(String token,long expiresInMinutes,long id,String name,String login,String role){}
 public record ResetRequest(@NotBlank @Email String email){}
 public record ResetPassword(@NotBlank String token,@NotBlank @Size(min=8,max=100) String newPassword,@NotBlank String confirmPassword){}
 public record PassengerInput(@NotBlank @Size(max=120) String name,@Min(0) @Max(120) int age,@NotBlank String gender,@Size(max=32) String passportNo,@NotBlank String verificationIdType,@NotBlank @Size(max=32) String verificationIdNumber,@NotBlank String seatNumber,@NotBlank String cabinClass){}
 public record CreateBooking(@Positive long flightId,@NotEmpty @Size(max=8) List<@jakarta.validation.Valid PassengerInput> passengers){}
 public record Pay(String paymentMethod){}
 public record CancellationRequestBody(@Positive long bookingId){}
 public record ResolveCancellation(boolean approve,String response){}
 public record AdminPasswordReset(@NotBlank @Size(min=8,max=100) String newPassword,@NotBlank String confirmPassword){}
 public record CreateFlight(@Positive long airlineId,@NotBlank String flightNumber,@NotBlank String departureCity,@NotBlank String destinationCity,@NotBlank String departureTime,@NotBlank String arrivalTime,@Min(0) int economyCapacity,@Min(0) int businessCapacity,@DecimalMin("0.00") BigDecimal economyPrice,@DecimalMin("0.00") BigDecimal businessPrice){}
}
