package edu.uespoo135.gt3.reservapi;

import edu.uespoo135.gt3.reservapi.dto.ReservaDTO;
import edu.uespoo135.gt3.reservapi.mapper.ReservaMapper;
import edu.uespoo135.gt3.reservapi.model.Cliente;
import edu.uespoo135.gt3.reservapi.model.Mesa;
import edu.uespoo135.gt3.reservapi.model.Reserva;
import edu.uespoo135.gt3.reservapi.model.Turno;
import edu.uespoo135.gt3.reservapi.repository.ClienteRepository;
import edu.uespoo135.gt3.reservapi.repository.MesaRepository;
import edu.uespoo135.gt3.reservapi.repository.ReservaRepository;
import edu.uespoo135.gt3.reservapi.repository.TurnoRepository;
import edu.uespoo135.gt3.reservapi.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;





@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepo;
    @Mock
    private ClienteRepository clienteRepo;
    @Mock
    private MesaRepository mesaRepo;
    @Mock
    private TurnoRepository turnoRepo;

    @InjectMocks
    private ReservaService service;

    @BeforeEach
    void setup() {
        // nothing for now
    }

    @Test
    void crear_shouldSaveAndReturnDto() {
        ReservaDTO dtoMock = mock(ReservaDTO.class);
        when(dtoMock.getClienteId()).thenReturn(10L);
        when(dtoMock.getMesaId()).thenReturn(20L);
        when(dtoMock.getTurnoId()).thenReturn(30L);
        when(dtoMock.getEstado()).thenReturn(null); // default to CREATED

        Cliente cliente = new Cliente();
        Mesa mesa = new Mesa();
        Turno turno = new Turno();

        when(clienteRepo.findById(10L)).thenReturn(Optional.of(cliente));
        when(mesaRepo.findById(20L)).thenReturn(Optional.of(mesa));
        when(turnoRepo.findById(30L)).thenReturn(Optional.of(turno));

        // Let save return the passed Reserva
        when(reservaRepo.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservaDTO expectedDto = mock(ReservaDTO.class);
        try (MockedStatic<ReservaMapper> mocked = mockStatic(ReservaMapper.class)) {
            mocked.when(() -> ReservaMapper.toDTO(any(Reserva.class))).thenReturn(expectedDto);

            ReservaDTO result = service.crear(dtoMock);

            assertSame(expectedDto, result);
            verify(reservaRepo, times(1)).save(any(Reserva.class));
            mocked.verify(() -> ReservaMapper.toDTO(any(Reserva.class)), times(1));
        }
    }

    @Test
    void crear_missingCliente_shouldThrow() {
        ReservaDTO dtoMock = mock(ReservaDTO.class);
        when(dtoMock.getClienteId()).thenReturn(5L);

        when(clienteRepo.findById(5L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.crear(dtoMock));
        assertTrue(ex.getMessage().contains("Cliente 5 no encontrado"));
    }

    @Test
    void listar_shouldMapAll() {
        Reserva r1 = new Reserva();
        Reserva r2 = new Reserva();
        when(reservaRepo.findAll()).thenReturn(List.of(r1, r2));

        ReservaDTO d1 = mock(ReservaDTO.class);
        ReservaDTO d2 = mock(ReservaDTO.class);

        try (MockedStatic<ReservaMapper> mocked = mockStatic(ReservaMapper.class)) {
            mocked.when(() -> ReservaMapper.toDTO(r1)).thenReturn(d1);
            mocked.when(() -> ReservaMapper.toDTO(r2)).thenReturn(d2);

            List<ReservaDTO> list = service.listar();

            assertEquals(2, list.size());
            assertTrue(list.contains(d1));
            assertTrue(list.contains(d2));
        }
    }

    @Test
    void obtener_existing_shouldReturnDto() {
        Reserva r = new Reserva();
        when(reservaRepo.findById(1L)).thenReturn(Optional.of(r));

        ReservaDTO dto = mock(ReservaDTO.class);
        try (MockedStatic<ReservaMapper> mocked = mockStatic(ReservaMapper.class)) {
            mocked.when(() -> ReservaMapper.toDTO(r)).thenReturn(dto);

            ReservaDTO result = service.obtener(1L);
            assertSame(dto, result);
        }
    }

    @Test
    void obtener_missing_shouldThrow() {
        when(reservaRepo.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.obtener(99L));
        assertTrue(ex.getMessage().contains("Reserva 99 no encontrada"));
    }

    @Test
    void confirmar_idempotentAlreadyConfirmed_shouldNotSave() {
        Reserva r = new Reserva();
        r.setEstado(Reserva.Estado.CONFIRMED);
        when(reservaRepo.findById(2L)).thenReturn(Optional.of(r));

        ReservaDTO dto = mock(ReservaDTO.class);
        try (MockedStatic<ReservaMapper> mocked = mockStatic(ReservaMapper.class)) {
            mocked.when(() -> ReservaMapper.toDTO(r)).thenReturn(dto);

            ReservaDTO result = service.confirmar(2L);
            assertSame(dto, result);
            verify(reservaRepo, never()).save(any());
        }
    }

    @Test
    void confirmar_fromCreated_shouldSaveAndReturn() {
        Reserva r = new Reserva();
        r.setEstado(Reserva.Estado.CREATED);
        when(reservaRepo.findById(3L)).thenReturn(Optional.of(r));
        when(reservaRepo.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservaDTO dto = mock(ReservaDTO.class);
        try (MockedStatic<ReservaMapper> mocked = mockStatic(ReservaMapper.class)) {
            mocked.when(() -> ReservaMapper.toDTO(any(Reserva.class))).thenReturn(dto);

            ReservaDTO result = service.confirmar(3L);

            assertSame(dto, result);
            assertEquals(Reserva.Estado.CONFIRMED, r.getEstado());
            verify(reservaRepo, times(1)).save(r);
        }
    }

    @Test
    void confirmar_invalidState_shouldThrow() {
        Reserva r = new Reserva();
        r.setEstado(Reserva.Estado.CANCELLED);
        when(reservaRepo.findById(4L)).thenReturn(Optional.of(r));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.confirmar(4L));
        assertTrue(ex.getMessage().contains("No se puede confirmar"));
    }

    @Test
    void cancelar_idempotentAlreadyCancelled_shouldNotSave() {
        Reserva r = new Reserva();
        r.setEstado(Reserva.Estado.CANCELLED);
        when(reservaRepo.findById(5L)).thenReturn(Optional.of(r));

        ReservaDTO dto = mock(ReservaDTO.class);
        try (MockedStatic<ReservaMapper> mocked = mockStatic(ReservaMapper.class)) {
            mocked.when(() -> ReservaMapper.toDTO(r)).thenReturn(dto);

            ReservaDTO result = service.cancelar(5L);
            assertSame(dto, result);
            verify(reservaRepo, never()).save(any());
        }
    }

    @Test
    void cancelar_fromCreated_shouldSaveAndReturn() {
        Reserva r = new Reserva();
        r.setEstado(Reserva.Estado.CREATED);
        when(reservaRepo.findById(6L)).thenReturn(Optional.of(r));
        when(reservaRepo.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservaDTO dto = mock(ReservaDTO.class);
        try (MockedStatic<ReservaMapper> mocked = mockStatic(ReservaMapper.class)) {
            mocked.when(() -> ReservaMapper.toDTO(any(Reserva.class))).thenReturn(dto);

            ReservaDTO result = service.cancelar(6L);

            assertSame(dto, result);
            assertEquals(Reserva.Estado.CANCELLED, r.getEstado());
            verify(reservaRepo, times(1)).save(r);
        }
    }

    @Test
    void cancelar_invalidState_shouldThrow() {
        Reserva r = new Reserva();
        r.setEstado(Reserva.Estado.COMPLETED);
        when(reservaRepo.findById(7L)).thenReturn(Optional.of(r));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.cancelar(7L));
        assertTrue(ex.getMessage().contains("No se puede cancelar"));
    }

    @Test
    void eliminar_nonExisting_shouldThrow() {
        when(reservaRepo.existsById(100L)).thenReturn(false);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.eliminar(100L));
        assertTrue(ex.getMessage().contains("Reserva 100 no encontrada"));
    }

    @Test
    void eliminar_existing_shouldDelete() {
        when(reservaRepo.existsById(11L)).thenReturn(true);
        service.eliminar(11L);
        verify(reservaRepo, times(1)).deleteById(11L);
    }
}