import { Component, inject } from '@angular/core';
import { MailComponent } from "../mail/mail.component";
import { UserService } from '../services/user-service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-uvod',
  imports: [MailComponent, RouterLink],
  templateUrl: './uvod.component.html',
  styleUrl: './uvod.component.scss'
})
export class UvodComponent {
  protected userService = inject(UserService);
}
