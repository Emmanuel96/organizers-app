import { Component } from '@angular/core';

@Component({
  standalone: true,
  selector: 'jhi-footer',
  templateUrl: './footer.component.html',
})
export default class FooterComponent {
  currentYear: number = new Date().getFullYear();
}
