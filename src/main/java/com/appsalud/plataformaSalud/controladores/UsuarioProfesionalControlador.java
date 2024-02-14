package com.appsalud.plataformaSalud.controladores;

import com.appsalud.plataformaSalud.entidades.Calendario;
import com.appsalud.plataformaSalud.entidades.DisponibilidadProfesional;
import com.appsalud.plataformaSalud.entidades.Turno;
import com.appsalud.plataformaSalud.entidades.UsuarioProfesional;
import com.appsalud.plataformaSalud.enumeraciones.Especialidad;
import com.appsalud.plataformaSalud.enumeraciones.ObraSocial;
import com.appsalud.plataformaSalud.excepciones.MiException;
import com.appsalud.plataformaSalud.servicios.CalendarioServicio;
import com.appsalud.plataformaSalud.servicios.DisponibilidadProfesionalServicio;
import com.appsalud.plataformaSalud.servicios.UsuarioProfesionalServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/profesional")
public class UsuarioProfesionalControlador {

    @Autowired
    private UsuarioProfesionalServicio usuarioProfesionalServicio;
    @Autowired
    private CalendarioServicio calendarioServicio;
    @Autowired
    private DisponibilidadProfesionalServicio disponibilidadProfesionalServicio;

    @PreAuthorize("hasRole('ROLE_PROFESIONAL')")
    @GetMapping("/dashboard-profesional")
    public String mostrarVistaProfesional(Model model) {
        List<Especialidad> listaEspecialidades = Arrays.asList(Especialidad.values());
        model.addAttribute("listaEspecialidades", listaEspecialidades);
        return "profesionalVista.html";
    }

    @GetMapping("/registrarProfesional")
    public String registroProfesional(Model model) {
        List<Especialidad> listaEspecialidades = Arrays.stream(Especialidad.values()).collect(Collectors.toList());
        List<ObraSocial> listaObrasSociales = Arrays.stream(ObraSocial.values()).collect(Collectors.toList());
        model.addAttribute("listaEspecialidades", listaEspecialidades);
        model.addAttribute("listaObrasSociales", listaObrasSociales);
        return "registroProfesional.html";
    }

    @PostMapping("/registroProfesional")
    public String registroProfesional(@RequestParam String nombre,
                                      @RequestParam String apellido,
                                      @RequestParam String email,
                                      @RequestParam String password,
                                      @RequestParam String password2,
                                      @RequestParam Especialidad especialidad,
                                      @RequestParam String descripcionEspecialidad,
                                      @RequestParam Integer valorConsulta,
                                      @RequestParam String matricula,
                                      @RequestParam String dni,
                                      @RequestParam String direccion,
                                      @RequestParam String telefono,
                                      @RequestParam List<ObraSocial> obrasSociales,
                                      RedirectAttributes redirectAttributes) {
        try {
            usuarioProfesionalServicio.crearUsuarioProfesional(nombre, apellido, email, password, password2, especialidad,
                    descripcionEspecialidad, valorConsulta, matricula, dni, direccion, telefono,
                    obrasSociales);

            redirectAttributes.addFlashAttribute("exito", "El Usuario fue registrado correctamente!");

            return "redirect:/";
        } catch (MiException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registroProfesional";
        }
    }

    @GetMapping("/dashboard-profesional/darBajaCuenta")
    public String darBajaCuentaProfesional(Model model) {
        return "darBajaCuentaProfesional.html";
    }

    @PostMapping("/darBaja")
    public String darBajaPaciente(Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            usuarioProfesionalServicio.anularProfesional(email);
            return "redirect:/dasboard-profesional";
        } catch (Exception e) {
            e.getMessage();
            return "redirect:/dashboard-profesional";
        }
    }

    @GetMapping("/dashboard-profesional/modificarProfesional")
    public String modificarProfesional(Model model) {
        List<Especialidad> listaEspecialidades = Arrays.asList(Especialidad.values());
        model.addAttribute("listaEspecialidades", listaEspecialidades);
        return "modificarProfesional.html";
    }

    @PostMapping("/profesionalForm")
    public String modificarProfesional(@RequestParam String nombre,
                                       @RequestParam String apellido,
                                       @RequestParam String passwordActual,
                                       @RequestParam String nuevoPassword,
                                       @RequestParam Especialidad especialidad,
                                       @RequestParam String descripcionEspecialidad,
                                       @RequestParam Integer valorConsulta,

                                       @RequestParam String dni,
                                       @RequestParam String direccion,
                                       @RequestParam String telefono,
                                       Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            if (!usuarioProfesionalServicio.verificarPassword(email, passwordActual)) {

                model.addAttribute("error", "La contraseña actual ingresada es incorrecta.");
                return "redirect:/profesional/dashboard-profesional/modificarProfesional";
            }

            // Si el password actual coincide, proceder con la modificación del usuario
            Optional<UsuarioProfesional> usuarioProfesionalOptional = usuarioProfesionalServicio
                    .buscarProfesionalPorEmail(email);
            if (usuarioProfesionalOptional.isPresent()) {
                UsuarioProfesional usuarioProfesional = usuarioProfesionalOptional.get();
                Integer reputacion = usuarioProfesional.getReputacion();
                String matricula = usuarioProfesional.getMatricula();
                usuarioProfesionalServicio.modificarProfesional(nombre, apellido, email, passwordActual, nuevoPassword,
                        especialidad, descripcionEspecialidad, reputacion, valorConsulta, matricula, dni, direccion, telefono,
                        Boolean.TRUE);
                model.addAttribute("exito", "Profesional modificado con exito");
                return "redirect:/profesional/dashboard-profesional";
            } else {
                model.addAttribute("error", "No se encontró ningún usuario profesional con el email proporcionado.");
                return "redirect:/profesional/dashboard-profesional"; // Puedes redirigir a la vista de modificar profesional o hacer lo que
                // consideres necesario
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard-profesional"; // aca redirigiria a la vista de modificar profesional.
    }
    @GetMapping("/dashboard-profesional/buscarTurnos")
    public String buscarTurnosProfesional(Model model) {
        return "buscarTurnosProfesional.html";
    }
    @GetMapping("/calendario")
    public String verCalendario(Model model) {
        // Obtener el usuario profesional actual desde la sesión
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<UsuarioProfesional> usuarioProfesionalOptional = usuarioProfesionalServicio
                .buscarProfesionalPorEmail(email);

        UsuarioProfesional usuarioProfesional = usuarioProfesionalOptional.get();
        // Obtener el calendario del profesional

        Calendario calendario = usuarioProfesional.getCalendario();

        // Obtener los turnos disponibles del profesional
        List<Turno> turnosDisponibles = calendarioServicio.obtenerTurnosDisponibles(usuarioProfesional);

        model.addAttribute("calendario", calendario);
        model.addAttribute("turnosDisponibles", turnosDisponibles);

        return "calendarioProfesional.html";
    }

    @GetMapping("/dashboard-profesional/establecer-disponibilidad")
    public String mostrarFormularioEstablecerDisponibilidad(Model model) {

        return "disponibilidad.html";
    }

    @PostMapping("/establecer-disponibilidad-form")
    public String establecerDisponibilidad(@RequestParam Map<String, String> formData, RedirectAttributes redirectAttributes) {
        try {
            // Obtener el profesional actual desde la sesión

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Optional<UsuarioProfesional> usuarioProfesionalOptional = usuarioProfesionalServicio
                    .buscarProfesionalPorEmail(email);

            UsuarioProfesional usuarioProfesional = usuarioProfesionalOptional.get();

            // Crear una lista para almacenar las disponibilidades
            List<DisponibilidadProfesional> disponibilidades = new ArrayList<>();

            // Iterar sobre el formData para obtener los datos de cada día de la semana
            for (DayOfWeek diaSemana : DayOfWeek.values()) {
                // Obtener los horarios de inicio y fin para este día de la semana
                String horaInicioStr = formData.get(diaSemana.toString().toLowerCase() + "Inicio");
                String horaFinStr = formData.get(diaSemana.toString().toLowerCase() + "Fin");


                // Convertir las cadenas de hora en LocalTime
                LocalTime horaInicio = LocalTime.parse(horaInicioStr);
                LocalTime horaFin = LocalTime.parse(horaFinStr);
                System.out.println("convirtio las cadenas de hora en LocalTime");


                // Crear una instancia de DisponibilidadProfesional y agregarla a la lista
                DisponibilidadProfesional disponibilidad = new DisponibilidadProfesional();
                disponibilidad.setUsuarioProfesional(usuarioProfesional);
                disponibilidad.setDiaSemana(diaSemana);
                disponibilidad.setHoraInicio(horaInicio);
                disponibilidad.setHoraFin(horaFin);
                disponibilidades.add(disponibilidad);
            }

            // Guardar las disponibilidades en la base de datos
            disponibilidadProfesionalServicio.guardarDisponibilidadProfesional(disponibilidades);
            disponibilidadProfesionalServicio.establecerDisponibilidad(usuarioProfesional, disponibilidades);

            redirectAttributes.addFlashAttribute("exito", "Disponibilidad establecida correctamente.");
            return "redirect:/profesional/dashboard-profesional";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hubo un problema al establecer la disponibilidad.");
            return "redirect:/profesional/dashboard-profesional";
        }
    }
}