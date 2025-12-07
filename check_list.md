# 📝 **Checklist Final – Aplicaciones Móviles**

## 📌 1. Organización previa

- [x] Elegir temática de la app (libre).
- [x] Formar equipo (opcional, máximo 2 personas).
- [x] Crear repositorio con este formato:  
       `final-am-ac[m|t|n]4[a|b|c|d]-apellido1-apellido2`
- [x] Los apellidos deben estar en **orden alfabético**.
- [x] Repo en **público**.
- [x] Agregar como colaborador a: **sergiomedinaio**.
- [ ] Preparar documento para DvPanel con:
  - Datos personales
  - Datos de cursada
  - Link al repositorio
  - Link al informe
- [ ] Subir a DvPanel **24 horas antes** de la evaluación.

## 🎯 2. Desarrollo mínimo obligatorio (nota 4)

Si falta algo de esta sección → **desaprobado automático**.

### **Estructura y navegación**

- [x] Varias pantallas (Activities).
- [x] Navegación correcta entre pantallas.

### **Informe**

- [ ] Descripción de cada pantalla.
- [ ] Funcionalidades de cada pantalla.
- [ ] Flujo de uso.
- [ ] Mockups (herramienta libre).

### **Layouts requeridos**

- [x] ConstraintLayout.
- [x] LinearLayout vertical.
- [x] LinearLayout horizontal.
- [x] ScrollView si hay contenido dinámico.

### **Widgets mínimos**

- [x] Buttons.
- [x] TextViews.
- [ ] Imágenes reales y contenido real.

### **Eventos e interacciones**

- [ ] OnClick y otros eventos reales.
- [x] Comportamiento dinámico (agregar, quitar, modificar elementos).
- [ ] Variables internas que reflejen los cambios visuales.

### **Pasaje de datos**

- [x] Uso de `Intent.putExtra()` entre Activities.

### **Firebase obligatorio**

- [x] Firebase Auth (login al menos).
- [x] Firestore (leer al menos un documento real).

## ⭐ 3. Agregados para llegar al 10

### **Firebase avanzado**

- [x] Registro + login.
- [x] Verificación de email (opcional).
- [x] Traer colección completa desde Firestore.

### **UI / UX**

- [ ] Elegir paleta de colores adecuada.
- [ ] Diseño consistente.
- [ ] Buena usabilidad.

### **Buenas prácticas**

- [ ] Código organizado en clases.
- [ ] Strings en `res/values/strings.xml`.
- [ ] Dimensiones en `res/values/dimens.xml`.
- [ ] Colores en `res/values/colors.xml`.

### **Características avanzadas**

- [ ] Descarga de imágenes desde URL.
- [x] Traer contenido desde internet con librerías (Glide, Retrofit, etc.).
- [ ] Notificaciones al usuario con Toast.
- [ ] Operaciones CRUD con Firestore:
  - [ ] Editar información del usuario.
  - [ ] Agregar documentos.
  - [ ] Relacionar datos entre colecciones.

### **Uso de commits**

- [x] Commits con convención (conventional commits).

### **Extras opcionales**

- [ ] Uso de patrón arquitectural (MVVM recomendado).
- [ ] Implementación de Google Maps.
- [ ] Base de datos local (Room).

## 🧪 4. Mesa evaluadora

- [x] Cada integrante debe presentarse.
- [x] Debe verse actividad real en tu cuenta de GitHub.
- [ ] Debés poder defender:
  - Código
  - Lógica
  - Integración con Firebase
  - Diseño
  - Flujo de la aplicación
