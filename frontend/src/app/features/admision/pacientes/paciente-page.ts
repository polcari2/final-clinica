import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ClinicApiService, Paciente } from '../../../core/services/clinic-api.service';

@Component({
  selector: 'app-paciente-page',
  imports: [ReactiveFormsModule],
  templateUrl: './paciente-page.html',
})
export class PacientePage implements OnInit {
  private readonly api = inject(ClinicApiService);
  private readonly fb = inject(FormBuilder);
  protected readonly items = signal<Paciente[]>([]);
  protected readonly editingId = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly page = signal(0);
  protected readonly pageSize = 10;
  protected readonly totalPages = signal(0);
  protected readonly totalElements = signal(0);

  protected readonly form = this.fb.nonNullable.group({
    dni: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(15)]],
    nombres: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(80)]],
    telefono: ['', Validators.maxLength(20)],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.api.pacientesPaginados(this.page(), this.pageSize).subscribe({
      next: (result) => {
        this.items.set(result.content);
        this.totalPages.set(result.totalPages);
        this.totalElements.set(result.totalElements);
      },
      error: () => {
        this.error.set('No se pudieron cargar los pacientes.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  editar(item: Paciente): void {
    this.editingId.set(item.id);
    this.form.setValue({
      dni: item.dni,
      nombres: item.nombres,
      apellidos: item.apellidos,
      telefono: item.telefono ?? '',
    });
    this.error.set(null);
    this.success.set(null);
  }

  cancelarEdicion(): void {
    this.editingId.set(null);
    this.form.reset();
  }

  guardar(): void {
    this.error.set(null);
    this.success.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const data = this.form.getRawValue();
    const id = this.editingId();
    this.loading.set(true);
    const request = id
      ? this.api.actualizarPaciente(id, data)
      : this.api.crearPaciente(data);
    request.subscribe({
      next: () => {
        this.success.set(id ? 'Paciente actualizado.' : 'Paciente registrado.');
        this.cancelarEdicion();
        this.page.set(0);
        this.cargar();
      },
      error: (response: HttpErrorResponse) => {
        this.error.set(response.status === 409 ? 'Ese DNI ya está registrado.' : 'No se pudo guardar el paciente.');
        this.loading.set(false);
      },
    });
  }

  eliminar(item: Paciente): void {
    if (!confirm(`¿Eliminar a ${item.nombres} ${item.apellidos}?`)) return;
    this.error.set(null);
    this.api.eliminarPaciente(item.id).subscribe({
      next: () => {
        this.success.set('Paciente eliminado.');
        if (this.items().length === 1 && this.page() > 0) {
          this.page.update((currentPage) => currentPage - 1);
        }
        this.cargar();
      },
      error: () => this.error.set('No se pudo eliminar. Puede tener citas asociadas.'),
    });
  }

  paginaAnterior(): void {
    if (this.page() > 0) {
      this.page.update((currentPage) => currentPage - 1);
      this.cargar();
    }
  }

  paginaSiguiente(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.page.update((currentPage) => currentPage + 1);
      this.cargar();
    }
  }
}
